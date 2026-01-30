package by.dreb.tutorhelper.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "payments",
    indices = [
        Index(value = ["lessonId"]),
        Index(value = ["paidOn"])
    ]
)
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val lessonId: Long,
    val studentId: Long = 0,
    val amount: Double,
    val paidOn: LocalDate?,
    val method: String?
)
