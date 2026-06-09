package com.catedra.bitacora.core.domain.useCase

import android.content.Context
import com.catedra.bitacora.R
import com.catedra.bitacora.core.helpers.NotificationHelper
import com.catedra.bitacora.features.travel.domain.model.PointOfInterest
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.ZoneId
import javax.inject.Inject

class SchedulePointPhotoReminderUseCase @Inject constructor(
    private val notificationHelper: NotificationHelper,
    @param:ApplicationContext private val context: Context
) {
    operator fun invoke(travelId: String, point: PointOfInterest, pointId: String) {
        val visitDate = point.visitDate ?: return
        // Programar para 1 hora despues de la visita
        val triggerMillis = visitDate
            .plusHours(1)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        if (triggerMillis > System.currentTimeMillis()) {
            notificationHelper.scheduleNotification(
                id = (pointId + "_photo").hashCode(),
                triggerAtMillis = triggerMillis,
                title = context.getString(R.string.point_photo_reminder_title),
                message = context.getString(R.string.point_photo_reminder_message, point.name),
                travelId = travelId,
                pointId = pointId
            )
        }
    }
}
