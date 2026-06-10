package com.catedra.bitacora.core.domain.useCase

import com.catedra.bitacora.R
import android.content.Context
import com.catedra.bitacora.core.helpers.NotificationHelper
import com.catedra.bitacora.features.travel.domain.model.Travel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.ZoneId
import javax.inject.Inject

class ScheduleTripStartNotificationUseCase @Inject constructor(
    private val notificationHelper: NotificationHelper,
    @param:ApplicationContext private val context: Context,
) {
    operator fun invoke(travel: Travel) {
        notificationHelper.cancelNotification(travel.id.hashCode())

        val startDate = travel.startDate ?: return
        val triggerMillis = startDate
            .atTime(9, 10)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        if (triggerMillis > System.currentTimeMillis()) {
            notificationHelper.scheduleNotification(
                id = travel.id.hashCode(),
                triggerAtMillis = triggerMillis,
                title = context.getString(R.string.your_travel_starts_today),
                message = context.getString(R.string.named_your_adventure_starts, travel.name),
                travelId = travel.id
            )
        }
    }
}