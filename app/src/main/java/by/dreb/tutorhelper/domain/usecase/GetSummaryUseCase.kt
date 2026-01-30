package by.dreb.tutorhelper.domain.usecase

import by.dreb.tutorhelper.domain.model.Summary
import by.dreb.tutorhelper.domain.repository.LessonRepository
import by.dreb.tutorhelper.domain.repository.PaymentRepository
import by.dreb.tutorhelper.domain.repository.StudentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetSummaryUseCase @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val studentRepository: StudentRepository,
    private val paymentRepository: PaymentRepository
) {
    operator fun invoke(): Flow<Summary> {
        return combine(
            lessonRepository.observeLessonDetails(),
            studentRepository.observeStudentsCount(false),
            studentRepository.observeStudentsCount(true),
            paymentRepository.observeTotalIncome(),
            paymentRepository.observePaymentsCount()
        ) { lessons, activeStudents, archivedStudents, incomeTotal, paidLessons ->
            Summary(
                lessonsCount = lessons.size,
                paidLessonsCount = paidLessons,
                studentsCount = activeStudents + archivedStudents,
                archivedStudentsCount = archivedStudents,
                incomeTotal = incomeTotal
            )
        }
    }
}
