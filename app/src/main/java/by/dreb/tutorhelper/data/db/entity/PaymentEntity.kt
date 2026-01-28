package by.dreb.tutorhelper.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

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
    val amount: Double,
    val paidOn: String?,
    val method: String?
)
