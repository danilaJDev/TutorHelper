package by.dreb.tutorhelper.domain.model

data class Student(
    val id: Long,
    val name: String,
    val phone: String?,
    val note: String?,
    val isArchived: Boolean,
    val defaultPrice: Double
)
