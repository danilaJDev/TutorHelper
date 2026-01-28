package by.dreb.tutorhelper.domain.model

data class Summary(
    val lessonsCount: Int,
    val paidLessonsCount: Int,
    val studentsCount: Int,
    val archivedStudentsCount: Int,
    val incomeTotal: Double
)
