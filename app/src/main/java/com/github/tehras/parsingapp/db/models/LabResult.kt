package com.github.tehras.parsingapp.db.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LabResult(val goal: Goal, val rows: List<Row>, val fileName: String, val error: Boolean = false) :
    Parcelable {
    companion object {
        fun error(fileName: String) = LabResult(Goal(0.0, 0.0), rows = listOf(), fileName = fileName, error = true)
    }
}

@Parcelize
data class Goal(val min: Double, val max: Double) : Parcelable

@Parcelize
data class Row(val startTime: Long, val value: Double?) : Parcelable