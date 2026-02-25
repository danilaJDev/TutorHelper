package by.dreb.tutorhelper.data.repository

import androidx.room.withTransaction
import by.dreb.tutorhelper.data.db.TutorHelperDatabase
import by.dreb.tutorhelper.data.db.dao.LessonDao
import by.dreb.tutorhelper.data.db.dao.PaymentDao
import by.dreb.tutorhelper.data.db.dao.StudentDao
import by.dreb.tutorhelper.data.mapper.toDomain
import by.dreb.tutorhelper.data.mapper.toEntity
import by.dreb.tutorhelper.di.ApplicationScope
import by.dreb.tutorhelper.domain.model.Lesson
import by.dreb.tutorhelper.domain.model.LessonDetails
import by.dreb.tutorhelper.domain.reminder.LessonReminderScheduler
import by.dreb.tutorhelper.domain.repository.LessonRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LessonRepositoryImpl @Inject constructor(
    private val lessonDao: LessonDao,
    private val paymentDao: PaymentDao,
    private val studentDao: StudentDao,
    private val database: TutorHelperDatabase,
    private val reminderScheduler: LessonReminderScheduler,
    @ApplicationScope private val applicationScope: CoroutineScope
) : LessonRepository {
    override fun observeLessons(): Flow<List<Lesson>> =
        lessonDao.observeLessons().map { entities -> entities.map { it.toDomain() } }

    override fun observeLessonDetails(): Flow<List<LessonDetails>> =
        lessonDao.observeLessonDetails()
            .map { entities -> autoCompleteLessons(entities.map { it.toDomain() }) }

    override fun observeLessonsByDate(date: LocalDate): Flow<List<LessonDetails>> =
        lessonDao.observeLessonDetailsByDate(date.toString())
            .map { entities -> autoCompleteLessons(entities.map { it.toDomain() }) }

    override fun observeLessonDetailsById(id: Long): Flow<LessonDetails?> =
        lessonDao.observeLessonDetailsById(id)
            .map { it?.toDomain() }
            .map { details ->
                details?.let { autoCompleteLesson(it) }
            }

    override suspend fun upsertLesson(lesson: Lesson) {
        val existingLesson = if (lesson.id != 0L) lessonDao.getLessonById(lesson.id)?.toDomain() else null
        val shouldResetCompletedStatus = existingLesson?.let { oldLesson ->
            oldLesson.isCompleted && oldLesson.startTime != lesson.startTime
        } ?: false

        val lessonToSave = if (shouldResetCompletedStatus) {
            lesson.copy(isCompleted = false)
        } else {
            lesson
        }

        val savedId = lessonDao.upsert(lessonToSave.toEntity())
        val lessonId = if (lessonToSave.id == 0L) savedId else lessonToSave.id
        val studentName = studentDao.getStudentById(lessonToSave.studentId)?.name ?: "Ученик"
        reminderScheduler.scheduleOrCancel(lessonToSave.copy(id = lessonId), studentName)
    }

    override suspend fun deleteLesson(lesson: Lesson) {
        database.withTransaction {
            transferPaymentToNextNearestLesson(lesson)
            lessonDao.delete(lesson.toEntity())
            reminderScheduler.cancel(lesson.id)
        }
    }

    override suspend fun deleteLessonWithFutureDuplicates(lesson: Lesson) {
        database.withTransaction {
            val lessons = lessonDao.getLessonsByStudentFrom(lesson.studentId, lesson.startTime.toString())
                .map { it.toDomain() }
            val targetTime = lesson.startTime.toLocalTime()
            val targetDay = lesson.startTime.dayOfWeek
            val candidatesToDelete = lessons.filter { candidate ->
                candidate.startTime.toLocalTime() == targetTime && candidate.startTime.dayOfWeek == targetDay
            }
            val excludedLessonIds = candidatesToDelete.map { it.id }.toSet()

            candidatesToDelete.forEach { candidate ->
                transferPaymentToNextNearestLesson(candidate, excludedLessonIds)
                lessonDao.delete(candidate.toEntity())
                reminderScheduler.cancel(candidate.id)
            }
        }
    }



    private suspend fun transferPaymentToNextNearestLesson(
        lesson: Lesson,
        excludedLessonIds: Set<Long> = emptySet()
    ) {
        val payment = paymentDao.getByLessonId(lesson.id) ?: return
        val nextLesson = lessonDao.getLessonDetailsByStudentAfter(lesson.studentId, lesson.startTime.toString())
            .map { it.toDomain() }
            .firstOrNull { details ->
                details.lesson.id !in excludedLessonIds && details.payment == null
            }

        if (nextLesson != null) {
            paymentDao.upsert(
                payment.copy(
                    lessonId = nextLesson.lesson.id,
                    studentId = nextLesson.student.id
                )
            )
        } else {
            paymentDao.deleteByLessonId(lesson.id)
        }
    }

    private fun autoCompleteLessons(lessons: List<LessonDetails>): List<LessonDetails> {
        val now = LocalDateTime.now()
        val toUpdate = lessons.filter { details ->
            !details.lesson.isCompleted && now.isAfter(details.lesson.endTime())
        }
        if (toUpdate.isNotEmpty()) {
            applicationScope.launch {
                toUpdate.forEach { details ->
                    lessonDao.upsert(details.lesson.copy(isCompleted = true).toEntity())
                }
            }
        }
        return lessons.map { details ->
            if (!details.lesson.isCompleted && now.isAfter(details.lesson.endTime())) {
                details.copy(lesson = details.lesson.copy(isCompleted = true))
            } else {
                details
            }
        }
    }

    private fun autoCompleteLesson(details: LessonDetails): LessonDetails {
        val now = LocalDateTime.now()
        if (!details.lesson.isCompleted && now.isAfter(details.lesson.endTime())) {
            applicationScope.launch {
                lessonDao.upsert(details.lesson.copy(isCompleted = true).toEntity())
            }
            return details.copy(lesson = details.lesson.copy(isCompleted = true))
        }
        return details
    }

    private fun Lesson.endTime(): LocalDateTime =
        startTime.plusMinutes(durationMinutes.toLong())
}
