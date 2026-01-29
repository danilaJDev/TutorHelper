package by.dreb.tutorhelper.domain.usecase

import by.dreb.tutorhelper.domain.repository.LessonRepository
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject

class GetScheduleUseCase @Inject constructor(
    private val lessonRepository: LessonRepository
) {
    operator fun invoke() = lessonRepository.observeLessonDetails().map { lessons ->
        val now = LocalDateTime.now()
        lessons.map { details ->
            val endTime = details.lesson.startTime.plusMinutes(details.lesson.durationMinutes.toLong())
            if (now.isAfter(endTime) && !details.lesson.isCompleted) {
                details.copy(lesson = details.lesson.copy(isCompleted = true))
            } else {
                details
            }
        }
    }
}
