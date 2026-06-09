package com.catedra.bitacora.core.domain.useCase

import com.catedra.bitacora.core.helpers.NotificationHelper
import com.catedra.bitacora.features.travel.domain.model.PointOfInterest
import java.time.ZoneId
import javax.inject.Inject

class SchedulePointVisitNotificationUseCase @Inject constructor(
    private val notificationHelper: NotificationHelper
) {
    operator fun invoke(travelId: String, point: PointOfInterest, pointId: String) {
        val visitDate = point.visitDate ?: return
        val triggerMillis = visitDate
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        if (triggerMillis > System.currentTimeMillis()) {
            notificationHelper.scheduleNotification(
                id = pointId.hashCode(),
                triggerAtMillis = triggerMillis,
                title = "Recordatorio de visita",
                message = "Es hora de tu parada en ${point.name}",
                travelId = travelId,
                pointId = pointId
            )
        }
    }
}