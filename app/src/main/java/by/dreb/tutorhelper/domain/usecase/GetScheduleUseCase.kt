package by.dreb.tutorhelper.domain.usecase

import by.dreb.tutorhelper.domain.repository.LessonRepository
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject

class GetScheduleUseCase @Inject constructor(
    private val lessonRepository: LessonRepository
) {
    operator fun invoke() = lessonRepository.observeLessonDetails()
}
