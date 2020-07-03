package ru.musintimur.storageapp.app

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import ru.musintimur.storageapp.R
import ru.musintimur.storageapp.model.room.ExchangeSchema
import ru.musintimur.storageapp.model.room.ImageData
import ru.musintimur.storageapp.model.room.ImageSource
import ru.musintimur.storageapp.model.room.product.Product
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

private const val TAG = "Files"
const val EXPORT_FILE_EXTENSION: String = "storageapp"

fun saveCompressedImage(context: Context, imageUri: Uri, imageName: String, quality: Int = 100): Uri {
    context.contentResolver.openInputStream(imageUri).use { inputStream ->
        val bitmap = try {
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            Log.e(TAG, context.getString(R.string.exception_decoding_stream))
            null
        }
        bitmap?.let {
            val directory = getImagesDirectory(context)
            val file = getFileInDir(directory, "$imageName.jpeg")

            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            }

            return Uri.fromFile(file)
        } ?: throw RuntimeException(context.getString(R.string.exception_saving_picture))
    }
}

fun exportData(context: Context, jsonData: String) {
    val directory = getDownloadsDirectory(context)
        ?: throw RuntimeException(context.getString(R.string.exception_downloads_directory))
    val file = getFileInDir(directory, "export-${System.currentTimeMillis()}.$EXPORT_FILE_EXTENSION")

    FileOutputStream(file).use { outputStream ->
        outputStream.write(jsonData.toByteArray())
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Files.FileColumns.DISPLAY_NAME, file.name)
            put(MediaStore.Files.FileColumns.MIME_TYPE, "text/json")
            put(
                MediaStore.Files.FileColumns.RELATIVE_PATH,
                Environment.DIRECTORY_DOWNLOADS
            )
        }

        resolver.insert(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            contentValues
        )?.let { uri ->
            resolver.openOutputStream(uri)?.use { outputStream ->
                FileInputStream(file).use { it.copyTo(outputStream, 1024) }
            }
        }

    } else {
        @Suppress("DEPRECATION") val destination =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .toString() + File.separator + file.name

        Uri.parse("file://$destination")?.let { uri ->
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                FileInputStream(file).use { it.copyTo(outputStream, 1024) }
            }
        }
    }
    file.delete()
    Toast.makeText(context, file.toString(), Toast.LENGTH_SHORT).show()
}

fun encodeImages(products: List<Product>): List<ImageData> {
    val images = mutableListOf<ImageData>()
    products.forEach { product ->
        if (product.imageUri.startsWith("http")) {
            images.add(ImageData(ImageSource.WEB, product.storeUuid, product.imageUri, null))
        } else {
            Uri.parse(product.imageUri).path?.let {
                val file = File(it)
                if (file.exists()) {
                    val bytes = file.readBytes()
                    val base64 = Base64.encodeToString(bytes, Base64.DEFAULT)
                    images.add(ImageData(ImageSource.LOCAL, product.storeUuid, file.name, base64))
                }
            }
        }
    }
    return images
}

fun importData(context: Context, jsonData: String): ExchangeSchema? =
    try {
        Gson().fromJson(jsonData, ExchangeSchema::class.java)
    } catch (e: Exception) {
        Log.e(TAG, context.getString(R.string.exception_parsing_data))
        null
    }

fun decodeImage(context: Context, imageData: ImageData): String? {
    if (imageData.source != ImageSource.LOCAL) return null

    val directory = getImagesDirectory(context)
    val file = getFileInDir(directory, imageData.name)

    val imageBytes = Base64.decode(imageData.base64, Base64.DEFAULT)
    file.writeBytes(imageBytes)
    return Uri.fromFile(file).toString()
}

fun readDocument(context: Context, uri: Uri): String {
    context.contentResolver.openInputStream(uri)?.use { inputStream ->
        return inputStream.bufferedReader().use(BufferedReader::readText)
    } ?: throw RuntimeException(context.getString(R.string.exception_reading_stream))
}

fun getImagesDirectory(context: Context): File {
    val directory = context.getDir("images", Context.MODE_PRIVATE)
    if (!directory.exists()) {
        directory.mkdirs()
    }
    return directory
}

fun getDownloadsDirectory(context: Context): File? {
    val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
    if (directory?.exists() == false) {
        directory.mkdirs()
    }
    return directory
}

fun getFileInDir(directory: File, name: String): File {
    val file = File(directory, name)
    if (file.exists()) {
        file.delete()
    }
    return file
}