package by.dreb.tutorhelper.data.backup

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import by.dreb.tutorhelper.data.db.TutorHelperDatabase
import by.dreb.tutorhelper.data.db.dao.LessonDao
import by.dreb.tutorhelper.data.db.dao.PaymentDao
import by.dreb.tutorhelper.data.db.dao.StudentDao
import by.dreb.tutorhelper.data.db.entity.LessonEntity
import by.dreb.tutorhelper.data.db.entity.PaymentEntity
import by.dreb.tutorhelper.data.db.entity.StudentEntity
import by.dreb.tutorhelper.domain.backup.BackupRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: TutorHelperDatabase,
    private val studentDao: StudentDao,
    private val lessonDao: LessonDao,
    private val paymentDao: PaymentDao
) : BackupRepository {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    override suspend fun exportToUri(uri: Uri) {
        val backup = BackupFile(
            students = studentDao.getAll().map { it.toBackup() },
            lessons = lessonDao.getAll().map { it.toBackup() },
            payments = paymentDao.getAll().map { it.toBackup() }
        )

        context.contentResolver.openOutputStream(uri)?.use { stream ->
            stream.write(json.encodeToString(BackupFile.serializer(), backup).encodeToByteArray())
        } ?: error("Не удалось открыть файл для экспорта")
    }

    override suspend fun importFromUri(uri: Uri) {
        val raw = context.contentResolver.openInputStream(uri)?.use { it.readBytes().decodeToString() }
            ?: error("Не удалось открыть файл для импорта")
        val backup = json.decodeFromString(BackupFile.serializer(), raw)

        database.withTransaction {
            paymentDao.clearAll()
            lessonDao.clearAll()
            studentDao.clearAll()

            backup.students.forEach { studentDao.upsert(it.toEntity()) }
            backup.lessons.forEach { lessonDao.upsert(it.toEntity()) }
            backup.payments.forEach { paymentDao.upsert(it.toEntity()) }
        }
    }
}

@Serializable
private data class BackupFile(
    val students: List<BackupStudent>,
    val lessons: List<BackupLesson>,
    val payments: List<BackupPayment>
)

@Serializable
private data class BackupStudent(
    val id: Long,
    val name: String,
    val phone: String? = null,
    val telegramUsername: String? = null,
    val viberPhone: String? = null,
    val whatsappPhone: String? = null,
    val note: String? = null,
    val isArchived: Boolean,
    val defaultPrice: Double
)

@Serializable
private data class BackupLesson(
    val id: Long,
    val studentId: Long,
    val subject: String,
    val startTime: String,
    val durationMinutes: Int,
    val price: Double,
    val note: String? = null,
    val reminderMinutesBefore: Int? = null,
    val isHidden: Boolean,
    val isHomeworkSent: Boolean,
    val isCompleted: Boolean
)

@Serializable
private data class BackupPayment(
    val id: Long,
    val lessonId: Long,
    val studentId: Long,
    val amount: Double,
    val paidOn: String? = null,
    val method: String
)

private fun StudentEntity.toBackup() = BackupStudent(
    id, name, phone, telegramUsername, viberPhone, whatsappPhone, note, isArchived, defaultPrice
)
private fun BackupStudent.toEntity() = StudentEntity(
    id, name, phone, telegramUsername, viberPhone, whatsappPhone, note, isArchived, defaultPrice
)
private fun LessonEntity.toBackup() = BackupLesson(
    id, studentId, subject, startTime, durationMinutes, price, note, reminderMinutesBefore, isHidden, isHomeworkSent, isCompleted
)
private fun BackupLesson.toEntity() = LessonEntity(
    id, studentId, subject, startTime, durationMinutes, price, note, reminderMinutesBefore, isHidden, isHomeworkSent, isCompleted
)
private fun PaymentEntity.toBackup() = BackupPayment(id, lessonId, studentId, amount, paidOn, method)
private fun BackupPayment.toEntity() = PaymentEntity(id, lessonId, studentId, amount, paidOn, method)
