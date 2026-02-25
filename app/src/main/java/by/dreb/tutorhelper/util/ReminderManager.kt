package by.dreb.tutorhelper.util

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

class ReminderManager(context: Context) {
    private val workManager = WorkManager.getInstance(context)

    fun scheduleLessonReminder(lessonId: Long, startTime: LocalDateTime) {
        val now = LocalDateTime.now()
        // Reminder 15 minutes before lesson
        val reminderTime = startTime.minusMinutes(15)

        if (reminderTime.isBefore(now)) return

        val delay = Duration.between(now, reminderTime).toMillis()

        val data = Data.Builder()
            .putLong("lessonId", lessonId)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<LessonReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        workManager.enqueueUniqueWork(
            getWorkName(lessonId),
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun cancelLessonReminder(lessonId: Long) {
        workManager.cancelUniqueWork(getWorkName(lessonId))
    }

    private fun getWorkName(lessonId: Long) = "lesson_reminder_$lessonId"
}
