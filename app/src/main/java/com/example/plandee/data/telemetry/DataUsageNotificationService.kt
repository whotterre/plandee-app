package com.example.plandee.data.telemetry

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.plandee.MainActivity
import com.example.plandee.R

object DataUsageNotificationService {

    private const val CHANNEL_ID = "plandee_data_alerts"
    private const val CHANNEL_NAME = "PlanDee Data Spend Alerts"
    private const val NOTIFICATION_ID = 5001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when mobile data consumption reaches 500MB milestones."
                enableVibration(true)
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    fun send500MbSpendNotification(context: Context, totalMbSpent: Double) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val mbFormatted = totalMbSpent.toInt()
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle("⚠️ PlanDee Data Alert")
            .setContentText("You have consumed $mbFormatted MB of mobile data in this session!")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Data Alert: You have passed the $mbFormatted MB mobile usage milestone. Tap to check your recommended tariff plan savings!")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.notify(NOTIFICATION_ID, notification)
    }
}
