package by.dreb.tutorhelper.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "students",
    indices = [
        Index(value = ["name"]),
        Index(value = ["isArchived"])
    ]
)
data class StudentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String?,
    val telegramUsername: String?,
    val viberPhone: String?,
    val whatsappPhone: String?,
    val note: String?,
    val isArchived: Boolean,
    val defaultPrice: Double = 0.0
)
