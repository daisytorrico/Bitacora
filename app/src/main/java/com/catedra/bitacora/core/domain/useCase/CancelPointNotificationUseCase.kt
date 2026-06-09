package com.catedra.bitacora.core.domain.useCase

import com.catedra.bitacora.core.helpers.NotificationHelper
import javax.inject.Inject

class CancelPointNotificationUseCase @Inject constructor(
    private val notificationHelper: NotificationHelper
) {
    operator fun invoke(pointId: String) {
        notificationHelper.cancelNotification(pointId.hashCode())
    }
}
