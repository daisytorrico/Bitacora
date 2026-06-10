package com.catedra.bitacora.core.domain.useCase

import com.catedra.bitacora.core.helpers.NotificationHelper
import javax.inject.Inject

class CancelPointNotificationUseCase @Inject constructor(
    private val notificationHelper: NotificationHelper
) {
    operator fun invoke(pointId: String) {
        // Cancelar notificación de visita
        notificationHelper.cancelNotification(pointId.hashCode())
        // Cancelar recordatorio de foto
        notificationHelper.cancelNotification((pointId + "_photo").hashCode())
    }
}
