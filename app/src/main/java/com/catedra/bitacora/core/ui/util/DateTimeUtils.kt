package com.catedra.bitacora.core.ui.util

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

//Retorna una representación relativa del tiempo

fun LocalDateTime.toRelativeTime(
    includePrefix: Boolean = false,
    shortUnits: Boolean = false
): String {
    val now = LocalDateTime.now()
    val minutes = ChronoUnit.MINUTES.between(this, now)
    val hours = minutes / 60
    val days = hours / 24

    val prefix = if (includePrefix) "hace " else ""
    
    return when {
        minutes < 1 -> "ahora"
        minutes < 60 -> {
            val unit = if (shortUnits) "min" else " min"
            "$prefix$minutes$unit"
        }
        hours < 24 -> {
            val unit = if (shortUnits) "h" else " h"
            "$prefix$hours$unit"
        }
        else -> {
            val unit = if (shortUnits) "d" else " d"
            "$prefix$days$unit"
        }
    }
}
