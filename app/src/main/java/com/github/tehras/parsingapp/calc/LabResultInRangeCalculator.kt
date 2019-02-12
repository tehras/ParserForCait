package com.github.tehras.parsingapp.calc

import com.github.tehras.dagger.scopes.ApplicationScope
import com.github.tehras.parsingapp.db.models.LabResult
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@ApplicationScope
class LabResultInRangeCalculator @Inject constructor() {
    fun calcuate(labResult: LabResult): LabResultInRange {
        if (labResult.error) {
            return LabResultInRange(0L, 0L, labResult.fileName, true)
        }
        val rows = labResult.rows
        val total = rows[rows.size - 1].startTime - rows[0].startTime
        var inRange = 0L
        var prevTimestamp = 0L
        var prevValue = 0.0

        val min = labResult.goal.min
        val max = labResult.goal.max

        rows.forEachIndexed { index, row ->
            if (index == rows.size - 1) {
                prevValue = row.value ?: prevValue
            }

            if (row.value != null && withinRange(min, max, prevValue)) {
                if (prevTimestamp != 0L) {
                    Timber.d("${TimeUnit.MILLISECONDS.toMinutes(row.startTime - prevTimestamp)} mins")
                    inRange += row.startTime - prevTimestamp
                }
            }

            prevValue = row.value ?: prevValue
            prevTimestamp = row.startTime
        }

        return LabResultInRange(inRange = inRange, total = total, fileName = labResult.fileName, error = false)
    }

    private fun withinRange(min: Double, max: Double, current: Double): Boolean {
        return current in min..max
    }
}

data class LabResultInRange(val inRange: Long, val total: Long, val fileName: String, val error: Boolean) {
    val percentage: Double by lazy { 100.times(inRange.toDouble()).div(total.toDouble()) }
}
