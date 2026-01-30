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

            val filteredPayments = payments.filter { payment ->
                val date = payment.paidOn ?: return@filter false
                (startDate == null || !date.isBefore(startDate)) &&
                (endDate == null || !date.isAfter(endDate))
            }

            val incomeTotal = filteredPayments.sumOf { it.amount }

            val paidLessonIds = filteredPayments.map { it.lessonId }.toSet()

            val filteredLessons = lessons.filter { details ->
                val date = details.lesson.startTime.toLocalDate()
                (startDate == null || !date.isBefore(startDate)) &&
                (endDate == null || !date.isAfter(endDate))
            }

            val allLessonIds = filteredLessons.map { it.lesson.id }.toSet() + paidLessonIds
            val lessonsCount = allLessonIds.size

            val paidLessonsCount = paidLessonIds.size

            val unpaidLessonsCount = filteredLessons.count { it.payment == null }

            val studentIdsFromPayments = filteredPayments.map { it.studentId }.filter { it != 0L }.toSet()
            val studentIdsFromLessons = filteredLessons.map { it.student.id }.toSet()

            val totalStudentsCount = if (startDate == null && endDate == null) {
                (allExistingStudents.map { it.id } + payments.map { it.studentId }.filter { it != 0L }).toSet().size
            } else {
                (studentIdsFromPayments + studentIdsFromLessons).size
            }

            val now = LocalDate.now()
            val yearsToShow = if (startDate == null && endDate == null) {
                listOf(now.year)
            } else {
                val startYear = startDate?.year ?: payments.minOfOrNull { it.paidOn?.year ?: now.year } ?: now.year
                val endYear = endDate?.year ?: now.year
                (startYear..endYear).toList()
            }

            val monthlyStats = yearsToShow.flatMap { year ->
                val yearPayments = filteredPayments.filter { it.paidOn?.year == year }
                val maxMonthIncome = (1..12).maxOfOrNull { m ->
                    yearPayments.filter { it.paidOn?.monthValue == m }.sumOf { it.amount }
                }?.takeIf { it > 0 } ?: 1.0

                (1..12).map { month ->
                    val income = yearPayments.filter { it.paidOn?.monthValue == month }.sumOf { it.amount }
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
