package com.example.meditrack.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateTimeUtils {
    private const val PATTERN_DATUM_VRIJEME = "dd.MM.yyyy HH:mm"
    private const val PATTERN_DATUM = "dd.MM.yyyy"

    fun parseDateTime(datum: String, vrijeme: String = ""): Date? {
        return try {
            if (vrijeme.isNotBlank()) {
                SimpleDateFormat(PATTERN_DATUM_VRIJEME, Locale.getDefault()).parse("$datum $vrijeme")
            } else {
                SimpleDateFormat(PATTERN_DATUM, Locale.getDefault()).parse(datum)
            }
        } catch (e: Exception) {
            null
        }
    }


    fun parseTimestamp(datum: String, vrijeme: String = ""): Long? =
        parseDateTime(datum, vrijeme)?.time
}