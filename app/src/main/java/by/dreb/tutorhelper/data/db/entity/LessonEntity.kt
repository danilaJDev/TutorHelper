package by.dreb.tutorhelper.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

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
    val startTime: LocalDateTime,
    val durationMinutes: Int,
    val price: Double,
    val note: String?,
    val isHidden: Boolean = false,
    val isHomeworkSent: Boolean = false,
    val isCompleted: Boolean = false
)
