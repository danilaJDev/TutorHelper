package by.dreb.tutorhelper.data.reminder

import android.content.Context
import androidx.hilt.android.qualifiers.ApplicationContext
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import by.dreb.tutorhelper.domain.model.Lesson
import by.dreb.tutorhelper.domain.reminder.LessonReminderScheduler
import javax.inject.Singleton
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject

@Singleton
class WorkManagerLessonReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) : LessonReminderScheduler {

    override fun scheduleOrCancel(lesson: Lesson, studentName: String) {
        val workName = workName(lesson.id)
        val reminderMinutes = lesson.reminderMinutesBefore
        if (lesson.id <= 0L || reminderMinutes == null || reminderMinutes <= 0) {
            WorkManager.getInstance(context).cancelUniqueWork(workName)
            return
        }

        val triggerTime = lesson.startTime.minusMinutes(reminderMinutes.toLong())
        if (!triggerTime.isAfter(LocalDateTime.now())) {
            WorkManager.getInstance(context).cancelUniqueWork(workName)
            return
        }

        val delay = Duration.between(LocalDateTime.now(), triggerTime)
        val request = OneTimeWorkRequestBuilder<LessonReminderWorker>()
            .setInputData(
                Data.Builder()
                    .putLong(LessonReminderWorker.KEY_LESSON_ID, lesson.id)
                    .putString(LessonReminderWorker.KEY_TITLE, "Скоро занятие")
                    .putString(
                        LessonReminderWorker.KEY_TEXT,
                        "$studentName через $reminderMinutes мин."
                    )
                    .build()
            )
            .setInitialDelay(delay)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(workName, ExistingWorkPolicy.REPLACE, request)
    }

    override fun cancel(lessonId: Long) {
        if (lessonId <= 0L) return
        WorkManager.getInstance(context).cancelUniqueWork(workName(lessonId))
    }

    private fun workName(lessonId: Long): String = "lesson_reminder_$lessonId"
}
