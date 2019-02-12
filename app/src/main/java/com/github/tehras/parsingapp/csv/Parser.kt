package com.github.tehras.parsingapp.csv

import com.github.tehras.dagger.scopes.ApplicationScope
import com.github.tehras.parsingapp.db.models.Goal
import com.github.tehras.parsingapp.db.models.LabResult
import com.github.tehras.parsingapp.db.models.Row
import de.siegmar.fastcsv.reader.CsvReader
import de.siegmar.fastcsv.reader.CsvRow
import io.reactivex.Single
import java.io.File
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


@ApplicationScope
class Parser @Inject constructor() {
    fun parseCsv(file: File): Single<LabResult> {
        return Single
            .fromCallable {
                var goal: Goal? = null
                val rows = arrayListOf<Row>()
                val csvReader = CsvReader()
                csvReader.parse(file, StandardCharsets.UTF_8).use { csvParser ->
                    var index = 0
                    var row: CsvRow? = csvParser.nextRow()
                    while (row != null) {
                        if (index == 0) { // Goal rows
                            val minAndMax = row.getField(2).split("-")
                            if (minAndMax.size != 2) {
                                throw RuntimeException("Must have minimum and max!")
                            }

                            goal = Goal(minAndMax[0].toDouble(), minAndMax[1].toDouble())
                        } else if (index > 1) {
                            val date = row.getField(0)
                            val time = row.getField(1)
                            val value = row.getField(2)

                            val startTime = SimpleDateFormat("M/dd/yy H:mm", Locale.US).parse("$date $time")
                            rows.add(Row(startTime = startTime.time, value = value.toDoubleOrNull()))
                        }

                        row = csvParser.nextRow()
                        index++
                    }
                }

                if (goal == null) throw Exception("Could not parse goal")

                LabResult(goal!!, rows, file.name)
            }
            .onErrorReturn { LabResult.error(fileName = file.name) }
    }
}