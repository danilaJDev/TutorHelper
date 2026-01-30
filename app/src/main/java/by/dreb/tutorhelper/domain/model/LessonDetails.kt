package by.dreb.tutorhelper.domain.model

import java.time.LocalDateTime

data class LessonDetails(
    val lesson: Lesson,
    val student: Student,
    val payment: Payment?
) {
    val isConducted: Boolean
        get() = LocalDateTime.now().isAfter(lesson.startTime.plusMinutes(lesson.durationMinutes.toLong()))
}
