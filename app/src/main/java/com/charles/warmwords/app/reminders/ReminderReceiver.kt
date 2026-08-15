package com.charles.warmwords.app.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.charles.warmwords.app.MainActivity
import com.charles.warmwords.app.R

const val REMINDER_CHANNEL_ID = "session_reminder_channel"

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, -1L)
        val label = intent.getStringExtra(EXTRA_LABEL) ?: "Time for your WarmWord session"

        ensureChannel(context)

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            reminderId.toInt(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("WarmWord session reminder")
            .setContentText(label)
            .setStyle(NotificationCompat.BigTextStyle().bigText(label))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        try {
            notificationManager.notify(reminderId.toInt(), notification)
        } catch (e: SecurityException) {
            // Notification permission not granted; skip silently.
        }
    }

    private fun ensureChannel(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            REMINDER_CHANNEL_ID,
            "Session Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Reminders for scheduled WarmWord sessions"
        }
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        const val EXTRA_REMINDER_ID = "reminder_id"
        const val EXTRA_LABEL = "label"
    }
}
