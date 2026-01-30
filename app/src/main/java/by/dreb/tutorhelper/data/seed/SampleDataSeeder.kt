package by.dreb.tutorhelper.data.seed

import by.dreb.tutorhelper.data.db.dao.LessonDao
import by.dreb.tutorhelper.data.db.dao.PaymentDao
import by.dreb.tutorhelper.data.db.dao.StudentDao
import by.dreb.tutorhelper.data.db.entity.LessonEntity
import by.dreb.tutorhelper.data.db.entity.PaymentEntity
import by.dreb.tutorhelper.data.db.entity.StudentEntity
import by.dreb.tutorhelper.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SampleDataSeeder @Inject constructor(
    private val studentDao: StudentDao,
    private val lessonDao: LessonDao,
    private val paymentDao: PaymentDao,
    @ApplicationScope private val scope: CoroutineScope
) {
    fun seedIfEmpty() {
        scope.launch {
            if (studentDao.countStudents() > 0) return@launch

            val aliceId = studentDao.upsert(
                StudentEntity(name = "Alice Petrova", phone = "+7 900 100 20 30", note = "ЕГЭ", isArchived = false, defaultPrice = 25.0)
            )
            val bobId = studentDao.upsert(
                StudentEntity(name = "Bob Smirnov", phone = "+7 900 555 66 77", note = "Math", isArchived = false, defaultPrice = 30.0)
            )
            val archivedId = studentDao.upsert(
                StudentEntity(name = "Irina Archive", phone = null, note = "Past student", isArchived = true)
            )

            val lesson1Id = lessonDao.upsert(
                LessonEntity(
                    studentId = aliceId,
                    subject = "English",
                    startTime = LocalDateTime.now().minusDays(1).withHour(16).withMinute(0).withSecond(0).withNano(0).toString(),
                    durationMinutes = 60,
                    price = 25.0,
                    note = "Grammar practice"
                )
            )
            val lesson2Id = lessonDao.upsert(
                LessonEntity(
                    studentId = bobId,
                    subject = "Mathematics",
                    startTime = LocalDateTime.now().plusDays(1).withHour(12).withMinute(0).withSecond(0).withNano(0).toString(),
                    durationMinutes = 90,
                    price = 30.0,
                    note = "Geometry"
                )
            )

            lessonDao.upsert(
                LessonEntity(
                    studentId = archivedId,
                    subject = "History",
                    startTime = LocalDateTime.now().minusWeeks(1).withHour(11).withMinute(0).withSecond(0).withNano(0).toString(),
                    durationMinutes = 45,
                    price = 20.0,
                    note = "Archived student"
                )
            )

            paymentDao.upsert(
                PaymentEntity(
                    lessonId = lesson1Id,
                    amount = 25.0,
                    paidOn = LocalDateTime.now().minusDays(1).toLocalDate().toString(),
                    method = "Cash"
                )
            )
            paymentDao.upsert(
                PaymentEntity(
                    lessonId = lesson2Id,
                    amount = 30.0,
                    paidOn = null,
                    method = "Card"
                )
            )
        }
    }
}
