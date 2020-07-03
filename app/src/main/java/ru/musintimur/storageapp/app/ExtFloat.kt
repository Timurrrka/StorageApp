package ru.musintimur.storageapp.app

import java.text.NumberFormat
import java.util.*

fun Float.asPrice(): String =
    NumberFormat.getCurrencyInstance(Locale("ru", "RU")).format(this)