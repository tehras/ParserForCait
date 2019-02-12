package com.github.tehras.parsingapp.ext

import android.content.Context
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.InputStream

fun Uri.convertUriToString(context: Context): String {
    val inputStream = context.contentResolver.openInputStream(this)!!

    return String(inputStreamToByteArray(inputStream)).also {
        inputStream.close()
    }
}

private fun inputStreamToByteArray(inputStream: InputStream): ByteArray {
    // this dynamically extends to take the bytes you read
    val byteBuffer = ByteArrayOutputStream()

    // this is storage overwritten on each iteration with bytes
    val bufferSize = 1024
    val buffer = ByteArray(bufferSize)

    // we need to know how may bytes were read to write them to the byteBuffer
    var len = inputStream.read(buffer)
    while (len != -1) {
        byteBuffer.write(buffer, 0, len)
        len = inputStream.read(buffer)
    }

    // and then we can return your byte array.
    return byteBuffer.toByteArray()
}

