package by.dreb.tutorhelper.domain.usecase

import by.dreb.tutorhelper.domain.model.MonthlyStat
import by.dreb.tutorhelper.domain.model.Summary
import by.dreb.tutorhelper.domain.repository.LessonRepository
import by.dreb.tutorhelper.domain.repository.PaymentRepository
import by.dreb.tutorhelper.domain.repository.StudentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import javax.inject.Inject

class GetSummaryUseCase @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val studentRepository: StudentRepository,
    private val paymentRepository: PaymentRepository
) {
    operator fun invoke(startDate: LocalDate? = null, endDate: LocalDate? = null): Flow<Summary> {
        return combine(
            lessonRepository.observeLessonDetails(),
            studentRepository.observeStudents(false),
            studentRepository.observeStudents(true),
            paymentRepository.observePayments()
        ) { lessons, activeStudents, archivedStudents, payments ->
            val allExistingStudents = activeStudents + archivedStudents
            val isWithinRange = { date: LocalDate ->
                (startDate == null || !date.isBefore(startDate)) &&
                    (endDate == null || !date.isAfter(endDate))
            }

            val filteredLessons = lessons.filter { details ->
                isWithinRange(details.lesson.startTime.toLocalDate())
            }

            val paymentsWithDate = payments.filter { it.paidOn != null }
            val filteredPayments = paymentsWithDate.filter { payment ->
                val date = payment.paidOn ?: return@filter false
                isWithinRange(date)
            }

            val paidCompletedLessons = filteredLessons.filter { details ->
                details.lesson.isCompleted && details.payment != null
            }

            val incomeTotal = paidCompletedLessons.sumOf { it.payment?.amount ?: 0.0 }

            val paidLessonIds = filteredPayments.map { it.lessonId }.toSet()

            val lessonsCount = if (startDate == null && endDate == null) {
                (filteredLessons.map { it.lesson.id }.toSet() + paidLessonIds).size
            } else {
                filteredLessons.size
            }

            val paidLessonsCount = if (startDate == null && endDate == null) {
                val paidLessonIdsFromLessons = filteredLessons.filter { it.payment != null }
                    .map { it.lesson.id }
                (paidLessonIds + paidLessonIdsFromLessons).size
            } else {
                filteredLessons.count { it.payment != null }
            }

            val unpaidLessonsCount = filteredLessons.count { it.payment == null }

            val studentIdsFromPayments =
                filteredPayments.map { it.studentId }.filter { it != 0L }.toSet()
            val studentIdsFromLessons = filteredLessons.map { it.student.id }.toSet()

            val totalStudentsCount = if (startDate == null && endDate == null) {
                (allExistingStudents.map { it.id } + payments.map { it.studentId }
                    .filter { it != 0L }).toSet().size
            } else {
                (studentIdsFromPayments + studentIdsFromLessons).size
            }

            val now = LocalDate.now()
            val yearsToShow = if (startDate == null && endDate == null) {
                val paymentYears = paymentsWithDate.mapNotNull { it.paidOn?.year }
                    .distinct()
                    .sorted()
                if (paymentYears.isNotEmpty()) paymentYears else listOf(now.year)
            } else {
                val startYear =
                    startDate?.year
                        ?: paymentsWithDate.minOfOrNull { it.paidOn?.year ?: now.year }
                        ?: now.year
                val endYear = endDate?.year ?: now.year
                (startYear..endYear).toList()
            }

            val monthlyStats = yearsToShow.flatMap { year ->
                val yearLessons = paidCompletedLessons.filter { it.lesson.startTime.year == year }
                val maxMonthIncome = (1..12).maxOfOrNull { m ->
                    yearLessons.filter { it.lesson.startTime.monthValue == m }
                        .sumOf { it.payment?.amount ?: 0.0 }
                }?.takeIf { it > 0 } ?: 1.0

                (1..12).map { month ->
                    val income = yearLessons.filter { it.lesson.startTime.monthValue == month }
                        .sumOf { it.payment?.amount ?: 0.0 }
                    MonthlyStat(year, month, income, maxMonthIncome)
                }
            }

            Summary(
                incomeTotal = incomeTotal,
                lessonsCount = lessonsCount,
                studentsCount = totalStudentsCount,
                paidLessonsCount = paidLessonsCount,
                unpaidLessonsCount = unpaidLessonsCount,
                monthlyStats = monthlyStats
            )
        }
    }
}
