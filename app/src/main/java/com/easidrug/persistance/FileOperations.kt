package com.easidrug.persistance

import android.content.Context
import java.io.File

fun Context.writeStringToFile(filename: String, data: String) {
    openFileOutput(filename, Context.MODE_PRIVATE).use {
        it.write(data.toByteArray())
    }
}

fun Context.readStringFromFile(filename: String): String {
    val file = File(filesDir, filename)

    if (!file.exists()) {
        return ""
    }
    return openFileInput(filename).bufferedReader().useLines { lines ->
        lines.fold("") { some, text ->
            "$some\n$text"
        }
    }
}