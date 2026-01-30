package by.dreb.tutorhelper.data.mapper

import by.dreb.tutorhelper.data.db.entity.LessonEntity
import by.dreb.tutorhelper.data.db.entity.LessonWithDetails
import by.dreb.tutorhelper.data.db.entity.PaymentEntity
import by.dreb.tutorhelper.data.db.entity.StudentEntity
import by.dreb.tutorhelper.domain.model.Lesson
import by.dreb.tutorhelper.domain.model.LessonDetails
import by.dreb.tutorhelper.domain.model.Payment
import by.dreb.tutorhelper.domain.model.Student
import java.time.LocalDate
import java.time.LocalDateTime

fun StudentEntity.toDomain() = Student(
    id = id,
    name = name,
    phone = phone,
    note = note,
    isArchived = isArchived,
    defaultPrice = defaultPrice
)

fun Student.toEntity() = StudentEntity(
    id = id,
    name = name,
    phone = phone,
    note = note,
    isArchived = isArchived,
    defaultPrice = defaultPrice
)

fun LessonEntity.toDomain() = Lesson(
    id = id,
    studentId = studentId,
    subject = subject,
    startTime = startTime,
    durationMinutes = durationMinutes,
    price = price,
    note = note,
    isHidden = isHidden,
    isHomeworkSent = isHomeworkSent,
    isCompleted = isCompleted
)

fun Lesson.toEntity() = LessonEntity(
    id = id,
    studentId = studentId,
    subject = subject,
    startTime = startTime,
    durationMinutes = durationMinutes,
    price = price,
    note = note,
    isHidden = isHidden,
    isHomeworkSent = isHomeworkSent,
    isCompleted = isCompleted
)

fun PaymentEntity.toDomain() = Payment(
    id = id,
    lessonId = lessonId,
    studentId = studentId,
    amount = amount,
    paidOn = paidOn,
    method = method
)

fun Payment.toEntity() = PaymentEntity(
    id = id,
    lessonId = lessonId,
    studentId = studentId,
    amount = amount,
    paidOn = paidOn,
    method = method
)

fun LessonWithDetails.toDomain() = LessonDetails(
    lesson = lesson.toDomain(),
    student = student.toDomain(),
    payment = payments.firstOrNull()?.toDomain()
)
