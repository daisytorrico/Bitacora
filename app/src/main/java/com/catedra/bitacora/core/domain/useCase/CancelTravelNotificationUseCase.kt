package com.catedra.bitacora.core.domain.useCase

import com.catedra.bitacora.core.helpers.NotificationHelper
import javax.inject.Inject

class CancelTravelNotificationUseCase @Inject constructor(
    private val notificationHelper: NotificationHelper
) {
    operator fun invoke(travelId: String) {
        // Cancelar notificacion de inicio de viaje
        notificationHelper.cancelNotification(travelId.hashCode())
        // Cancelar notificacion de preparación 1 dia antes
        notificationHelper.cancelNotification((travelId + "_prep").hashCode())
    }
}
