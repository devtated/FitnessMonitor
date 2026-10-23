package com.example.fitnessmonitor.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

object NotificationHelper {
    private const val CHANNEL_ID = "fitness_goal_channel"
    private const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Fitness Goal Notifications"
            val descriptionText = "Notifications when you reach your daily activity goal"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showGoalAchievedNotification(context: Context, steps: Int, goal: Int) {
        createNotificationChannel(context)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🎉 Daily Goal Achieved!")
            .setContentText("Amazing job! You reached $steps steps (Goal: $goal). Keep up the active lifestyle!")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Amazing job! You reached $steps steps (Goal: $goal). Keep up the active lifestyle and stay healthy!"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        try {
            notificationManager.notify(NOTIFICATION_ID, builder.build())
        } catch (_: SecurityException) {
            // Handle missing notification permission on Android 13+ gracefully
        }
    }
}
