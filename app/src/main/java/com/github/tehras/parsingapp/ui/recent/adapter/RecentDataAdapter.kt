package com.github.tehras.parsingapp.ui.recent.adapter

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.tehras.base.ext.views.viewHolderFromParent
import com.github.tehras.parsingapp.R
import com.github.tehras.parsingapp.calc.LabResultInRange
import com.github.tehras.parsingapp.ui.recent.RecentDataUiEvent
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.list_item_result.*
import java.util.concurrent.TimeUnit

class RecentDataAdapter(private val consumer: Consumer<RecentDataUiEvent>) :
    RecyclerView.Adapter<RecentDataViewHolder>(), Consumer<List<LabResultInRange>> {
    private val inRange = mutableListOf<LabResultInRange>()

    override fun accept(t: List<LabResultInRange>) {
        inRange.clear()
        inRange.addAll(t)

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentDataViewHolder {
        return RecentDataViewHolder(parent.viewHolderFromParent(R.layout.list_item_result), consumer)
    }

    override fun getItemCount(): Int = inRange.size

    override fun onBindViewHolder(holder: RecentDataViewHolder, position: Int) {
        holder.bind(inRange[position])
    }
}

class RecentDataViewHolder(override val containerView: View, val consumer: Consumer<RecentDataUiEvent>) :
    RecyclerView.ViewHolder(containerView), LayoutContainer {
    private var disposable: Disposable? = null

    @SuppressLint("SetTextI18n")
    fun bind(labResultInRange: LabResultInRange) {
        recent_li_file_name.text = labResultInRange.fileName

        disposable?.dispose()
        disposable = clickable_layout
            .clicks()
            .throttleFirst(100, TimeUnit.MILLISECONDS)
            .map { RecentDataUiEvent.FileClicked(labResultInRange.fileName) }
            .subscribe(consumer)

        if (labResultInRange.error) {
            recent_li_in_range.text = ""
            recent_li_total.text = ""
            recent_li_in_range_percent.text = "Error Parsing"
        } else {
            recent_li_in_range.text = "${TimeUnit.MILLISECONDS.toMinutes(labResultInRange.inRange)} mins"
            recent_li_total.text = "${TimeUnit.MILLISECONDS.toMinutes(labResultInRange.total)} mins"
            recent_li_in_range_percent.text = "${"%.2f".format(labResultInRange.percentage)}%"
        }
    }
}