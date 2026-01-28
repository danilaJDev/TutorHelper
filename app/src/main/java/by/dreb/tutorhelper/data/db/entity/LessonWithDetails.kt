package by.dreb.tutorhelper.data.db.entity

import androidx.room.Embedded
import androidx.room.Relation

data class LessonWithDetails(
    @Embedded val lesson: LessonEntity,
    @Relation(parentColumn = "studentId", entityColumn = "id")
    val student: StudentEntity,
    @Relation(parentColumn = "id", entityColumn = "lessonId")
    val payments: List<PaymentEntity>
)
