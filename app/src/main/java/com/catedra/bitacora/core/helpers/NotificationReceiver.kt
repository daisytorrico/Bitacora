 package com.catedra.bitacora.core.helpers

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import com.catedra.bitacora.MainActivity
import com.catedra.bitacora.R

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(NotificationHelper.EXTRA_TITLE) ?: return
        val message = intent.getStringExtra(NotificationHelper.EXTRA_MESSAGE) ?: return
        val travelId = intent.getStringExtra(NotificationHelper.EXTRA_TRAVEL_ID) ?: ""
        val pointId = intent.getStringExtra(NotificationHelper.EXTRA_POINT_ID)

        val uri = if (pointId != null) {
            "bitacora://travel_details/$travelId/points/$pointId"
        } else {
            "bitacora://travel_detail/$travelId"
        }

        val deepLinkIntent = Intent(context, MainActivity::class.java).apply {
            data = Uri.parse(uri)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            deepLinkIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}