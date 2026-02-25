package by.dreb.tutorhelper.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import by.dreb.tutorhelper.MainActivity
import by.dreb.tutorhelper.R

class NotificationHelper(private val context: Context) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_reminders),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders for upcoming lessons"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showLessonNotification(lessonId: Long, studentName: String, time: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // In a real app we would navigate to lesson details
            // putExtra("lessonId", lessonId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            lessonId.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Use default icon
            .setContentTitle(context.getString(R.string.notification_upcoming_lesson_title))
            .setContentText(context.getString(R.string.notification_upcoming_lesson_desc, studentName, time))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(lessonId.toInt(), notification)
    }

    companion object {
        const val CHANNEL_ID = "lesson_reminders"
    }
}
