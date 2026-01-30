package by.dreb.tutorhelper.domain.model

import java.time.LocalDate

data class Payment(
    val id: Long,
    val lessonId: Long,
    val studentId: Long,
    val amount: Double,
    val paidOn: LocalDate?,
    val method: String?
)
