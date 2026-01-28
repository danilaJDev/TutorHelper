package by.dreb.tutorhelper.domain.model

data class LessonDetails(
    val lesson: Lesson,
    val student: Student,
    val payment: Payment?
)
