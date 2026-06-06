package com.catedra.bitacora.core.domain.useCase

import com.catedra.bitacora.core.helpers.NotificationHelper
import com.catedra.bitacora.features.travel.domain.model.Travel
import java.time.ZoneId
import javax.inject.Inject

class ScheduleTripStartNotificationUseCase @Inject constructor(
    private val notificationHelper: NotificationHelper
) {
    operator fun invoke(travel: Travel) {
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
                title = "¡Hoy empieza tu viaje!",
                message = "Tu aventura '${travel.name}' arranca hoy. ¡Buen viaje!",
                travelId = travel.id
            )
        }
    }
}