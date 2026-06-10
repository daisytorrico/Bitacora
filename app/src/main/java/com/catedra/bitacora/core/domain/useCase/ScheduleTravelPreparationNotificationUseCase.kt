package com.catedra.bitacora.core.domain.useCase

import android.content.Context
import com.catedra.bitacora.R
import com.catedra.bitacora.core.helpers.NotificationHelper
import com.catedra.bitacora.features.travel.domain.model.Travel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.ZoneId
import javax.inject.Inject

class ScheduleTravelPreparationNotificationUseCase @Inject constructor(
    private val notificationHelper: NotificationHelper,
    @param:ApplicationContext private val context: Context
) {
    operator fun invoke(travel: Travel) {
        val notificationId = (travel.id + "_prep").hashCode()
        notificationHelper.cancelNotification(notificationId)

        val startDate = travel.startDate ?: return
        // Programar para 24 horas antes a las 10:00
        val triggerMillis = startDate
            .minusDays(1)
            .atTime(10, 0)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        if (triggerMillis > System.currentTimeMillis()) {
            notificationHelper.scheduleNotification(
                id = notificationId,
                triggerAtMillis = triggerMillis,
                title = context.getString(R.string.travel_prep_title),
                message = context.getString(R.string.travel_prep_message, travel.name),
                travelId = travel.id
            )
        }
    }
}
