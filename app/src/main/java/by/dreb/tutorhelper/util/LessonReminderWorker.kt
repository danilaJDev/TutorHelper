package by.dreb.tutorhelper.util

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import by.dreb.tutorhelper.domain.repository.LessonRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.format.DateTimeFormatter

@HiltWorker
class LessonReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val lessonRepository: LessonRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val lessonId = inputData.getLong("lessonId", -1L)
        if (lessonId == -1L) return Result.failure()

        val lessonDetails = lessonRepository.getLessonDetailsById(lessonId) ?: return Result.success()
        if (lessonDetails.lesson.isCompleted) return Result.success()

        val notificationHelper = NotificationHelper(applicationContext)
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        notificationHelper.showLessonNotification(
            lessonId = lessonId,
            studentName = lessonDetails.student.name,
            time = lessonDetails.lesson.startTime.format(timeFormatter)
        )

        return Result.success()
    }
}
