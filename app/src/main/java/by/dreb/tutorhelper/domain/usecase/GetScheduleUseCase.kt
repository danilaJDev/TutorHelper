package by.dreb.tutorhelper.domain.usecase

import by.dreb.tutorhelper.domain.repository.LessonRepository
import javax.inject.Inject

class GetScheduleUseCase @Inject constructor(
    private val lessonRepository: LessonRepository
) {
    operator fun invoke() = lessonRepository.observeLessonDetails()
}
