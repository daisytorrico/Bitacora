package com.catedra.bitacora.core.ui.util

import com.catedra.bitacora.R
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

fun LocalDateTime.toRelativeTime(
    includePrefix: Boolean = false,
    shortUnits: Boolean = false
): UiText {
    val now = LocalDateTime.now()
    val minutes = ChronoUnit.MINUTES.between(this, now)
    val hours = minutes / 60
    val days = hours / 24

    return when {
        minutes < 1 -> UiText.StringResource(R.string.time_now)
        
        minutes < 60 -> {
            val unitRes = if (shortUnits) R.string.unit_min else R.string.unit_min_long
            buildRelativeText(minutes, unitRes, includePrefix)
        }
        
        hours < 24 -> {
            val unitRes = if (shortUnits) R.string.unit_hour else R.string.unit_hour_long
            buildRelativeText(hours, unitRes, includePrefix)
        }
        
        else -> {
            val unitRes = if (shortUnits) R.string.unit_day else R.string.unit_day_long
            buildRelativeText(days, unitRes, includePrefix)
        }
    }
}

private fun buildRelativeText(
    value: Long,
    unitRes: Int,
    includePrefix: Boolean
): UiText {
    val textWithUnit = UiText.StringResource(unitRes, value)
    
    return if (includePrefix) {
        UiText.StringResource(R.string.time_ago, textWithUnit)
    } else {
        textWithUnit
    }
}
