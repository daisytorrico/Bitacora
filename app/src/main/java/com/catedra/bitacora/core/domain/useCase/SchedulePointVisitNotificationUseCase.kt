package com.catedra.bitacora.core.domain.useCase

import android.content.Context
import com.catedra.bitacora.R
import com.catedra.bitacora.core.helpers.NotificationHelper
import com.catedra.bitacora.features.travel.domain.model.PointOfInterest
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.ZoneId
import javax.inject.Inject

class SchedulePointVisitNotificationUseCase @Inject constructor(
    private val notificationHelper: NotificationHelper,
    @param:ApplicationContext private val context: Context
) {
    operator fun invoke(travelId: String, point: PointOfInterest, pointId: String) {
        val notificationId = pointId.hashCode()
        notificationHelper.cancelNotification(notificationId)

        val visitDate = point.visitDate ?: return
        val triggerMillis = visitDate
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        if (triggerMillis > System.currentTimeMillis()) {
            notificationHelper.scheduleNotification(
                id = notificationId,
                triggerAtMillis = triggerMillis,
                title = context.getString(R.string.visit_reminder_title),
                message = context.getString(R.string.visit_reminder_message, point.name),
                travelId = travelId,
                pointId = pointId
            )
        }
    }
}
