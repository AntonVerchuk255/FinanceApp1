package com.example.financeapp.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.example.financeapp.MainActivity
import com.example.financeapp.R
import com.example.financeapp.data.AppDatabase
import com.example.financeapp.ui.dashboard.formatAmount
import java.time.LocalDate
import java.util.concurrent.TimeUnit

class ReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getInstance(context)
        val today = LocalDate.now().dayOfMonth
        val reminders = db.reminderDao().getActiveRemindersByDay(today)

        if (reminders.isEmpty()) return Result.success()

        createNotificationChannel()

        reminders.forEach { reminder ->
            showNotification(
                id = reminder.id.toInt(),
                title = reminder.title,
                message = "Сегодня платёж: ${formatAmount(reminder.amount)}"
            )
        }

        return Result.success()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Напоминания о платежах",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Уведомления о регулярных платежах"
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun showNotification(id: Int, title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(id, notification)
    }

    companion object {
        const val CHANNEL_ID = "reminders_channel"

        fun schedule(context: Context) {
            // Запускаем каждый день в 9:00
            val now = java.time.LocalTime.now()
            val targetHour = 9
            val minutesUntilTarget = if (now.hour < targetHour) {
                (targetHour - now.hour) * 60L - now.minute
            } else {
                (24 - now.hour + targetHour) * 60L - now.minute
            }

            val dailyWork = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(minutesUntilTarget, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "daily_reminder",
                ExistingPeriodicWorkPolicy.KEEP,
                dailyWork
            )
        }
    }
}