package by.dreb.tutorhelper.domain.model

import java.time.LocalDateTime

data class Lesson(
    val id: Long,
    val studentId: Long,
    val subject: String,
    val startTime: LocalDateTime,
    val durationMinutes: Int,
    val price: Double,
    val note: String?,
    val isHidden: Boolean = false,
    val isHomeworkSent: Boolean = false,
    val isCompleted: Boolean = false
)
