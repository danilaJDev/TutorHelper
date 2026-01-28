package by.dreb.tutorhelper.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lessons",
    indices = [
        Index(value = ["studentId"]),
        Index(value = ["startTime"])
    ]
)
data class LessonEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: Long,
    val subject: String,
    val startTime: String,
    val durationMinutes: Int,
    val price: Double,
    val note: String?
)
