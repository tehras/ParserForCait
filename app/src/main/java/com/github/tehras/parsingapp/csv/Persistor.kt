package com.github.tehras.parsingapp.csv

import android.content.Context
import com.github.tehras.dagger.scopes.ApplicationScope
import io.reactivex.Single
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@ApplicationScope
class Persistor @Inject constructor(private val context: Context) {

    fun loadFiles(): Single<List<File>> {
        return Single.fromCallable { fileDir().listFiles()?.toList() ?: emptyList() }
    }

    fun saveFile(content: String, fileName: String?): File {
        val file = File(fileDir(), fileName ?: fileName())
        file.writeText(content)

        return file
    }

    private fun fileName(): String {
        return "${SimpleDateFormat("YYYY-MM-dd-hh-mm", Locale.US).format(Date())}.csv"
    }

    private fun fileDir(): File {
        return File((context.externalCacheDir ?: context.cacheDir), "csv").apply {
            if (!exists()) mkdirs()
        }
    }
}