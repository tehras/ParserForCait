package com.github.tehras.parsingapp.ui.recent

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.github.tehras.base.arch.ObservableViewModel
import com.github.tehras.dagger.scopes.FragmentScope
import com.github.tehras.parsingapp.calc.LabResultInRange
import com.github.tehras.parsingapp.calc.LabResultInRangeCalculator
import com.github.tehras.parsingapp.csv.Parser
import com.github.tehras.parsingapp.csv.Persistor
import com.github.tehras.parsingapp.db.models.LabResult
import com.github.tehras.parsingapp.ext.convertUriToString
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.io.File
import javax.inject.Inject

@FragmentScope
class RecentDataViewModel @Inject constructor(
    private val parser: Parser,
    private val persistor: Persistor,
    private val context: Context,
    private val labResultInRangeCalculator: LabResultInRangeCalculator
) :
    ObservableViewModel<RecentDataState, RecentDataUiEvent>() {

    private val fetchFile = PublishRelay.create<Unit>()

    override fun onCreate() {
        super.onCreate()

        uiEvents()
            .ofType<RecentDataUiEvent.FileChosen>()
            .subscribeOn(Schedulers.io())
            .map { it.path }
            .flatMapSingle(::saveToFile)
            .subscribeBy { fetchFile.accept(Unit) }

        val filesObservable = fetchFile
            .flatMapSingle {
                persistor
                    .loadFiles()
                    .flatMap { parseFiles(it) }
            }
            .flatMapSingle(::calculateResult)

        val openFile = uiEvents()
            .ofType<RecentDataUiEvent.FileClicked>()
            .map { it.fileName }
            .flatMapSingle { fileName ->
                persistor
                    .loadFiles()
                    .map { Pair(fileName, it) }
            }
            .map { (fileName, results) ->
                val result = results.firstOrNull { it.name == fileName }
                if (result == null) {
                    OpenFile.None
                } else {
                    OpenFile.CsvFile(result)
                }
            }
            .flatMap { Observable.just(it, OpenFile.None) }
            .startWith(OpenFile.None)

        Observables
            .combineLatest(filesObservable, openFile)
            .subscribeOn(Schedulers.computation())
            .map { RecentDataState(it.first, it.second) }
            .subscribeUntilDestroyed()
    }

    override fun onStart() {
        super.onStart()

        fetchFile.accept(Unit)
    }

    private fun calculateResult(labResults: List<LabResult>): Single<List<LabResultInRange>> {
        return Single.fromCallable { labResults.map(labResultInRangeCalculator::calcuate) }
    }

    private fun parseFiles(files: List<File>): Single<List<LabResult>> {
        val parsed: Iterable<Single<LabResult>> = files
            .map {
                parser.parseCsv(it)
            }
        return Single.zip(parsed) { result ->
            result
                .filter { it != null }
                .map { it as LabResult }
        }
    }

    private fun saveToFile(uri: Uri): Single<File> {
        return Single.fromCallable {
            val fileName = DocumentFile.fromSingleUri(context, uri)?.name
            val csv = uri.convertUriToString(context = context)
            persistor.saveFile(csv, fileName)
        }
    }
}

data class RecentDataState(val results: List<LabResultInRange>, val openFile: OpenFile)

sealed class OpenFile {
    object None : OpenFile()
    data class CsvFile(val file: File) : OpenFile()
}

sealed class RecentDataUiEvent {
    data class FileChosen(val path: Uri) : RecentDataUiEvent()
    data class FileClicked(val fileName: String) : RecentDataUiEvent()
}