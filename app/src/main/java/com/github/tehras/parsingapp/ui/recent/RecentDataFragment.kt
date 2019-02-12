package com.github.tehras.parsingapp.ui.recent

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.tehras.base.arch.viewModel
import com.github.tehras.base.dagger.components.findComponent
import com.github.tehras.parsingapp.R
import com.github.tehras.parsingapp.ui.recent.adapter.RecentDataAdapter
import com.jakewharton.rxbinding3.appcompat.navigationClicks
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.fragment_recent_data.*
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class RecentDataFragment : Fragment() {
    companion object {
        private const val REQUEST_CODE_PICK_CSV = 69

        fun instance() = RecentDataFragment()
    }

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private val viewModel by viewModel<RecentDataViewModel> { factory }
    private val createDisposable = CompositeDisposable()

    private val recentsAdapter by lazy { RecentDataAdapter(viewModel) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        findComponent<RecentDataComponentCreator>()
            .plusRecentDataComponent()
            .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_recent_data, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initToolbar()
        initRv()

        createDisposable += recent_data_new_csv
            .clicks()
            .throttleFirst(100, TimeUnit.MILLISECONDS)
            .subscribe { launchCsvPicker() }

        createDisposable += viewModel.observeState()
            .map { it.results }
            .distinctUntilChanged()
            .subscribe(recentsAdapter)

        createDisposable += viewModel.observeState()
            .map { it.openFile }
            .filter { it != OpenFile.None }
            .map { (it as OpenFile.CsvFile).file }
            .subscribe { openFile(it) }
    }

    private fun openFile(file: File) {
        val intent = Intent()
        intent.action = android.content.Intent.ACTION_VIEW
        intent.setDataAndType(Uri.fromFile(file), "text/csv")
        startActivity(intent)
    }

    override fun onDestroyView() {
        createDisposable.clear()

        super.onDestroyView()
    }

    private fun initToolbar() {
        createDisposable += recent_toolbar
            .navigationClicks()
            .throttleFirst(100, TimeUnit.MILLISECONDS)
            .subscribe {
                activity?.finish()
            }
    }

    private fun initRv() {
        recent_list_view.run {
            adapter = recentsAdapter
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
    }

    private fun launchCsvPicker() {
        startActivityForResult(
            Intent(Intent.ACTION_GET_CONTENT)
                .apply {
                    type = "text/csv"
                    addCategory(Intent.CATEGORY_OPENABLE)
                }, REQUEST_CODE_PICK_CSV
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            REQUEST_CODE_PICK_CSV -> {
                data?.data?.let {
                    viewModel.accept(RecentDataUiEvent.FileChosen(it))
                } ?: kotlin.run {
                    Toast.makeText(context, "Could not open the file", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}