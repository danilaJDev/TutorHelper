package by.dreb.tutorhelper.domain.usecase

import by.dreb.tutorhelper.domain.model.MonthlyStat
import by.dreb.tutorhelper.domain.model.Summary
import by.dreb.tutorhelper.domain.repository.LessonRepository
import by.dreb.tutorhelper.domain.repository.StudentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import javax.inject.Inject

class GetSummaryUseCase @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val studentRepository: StudentRepository
) {
    operator fun invoke(startDate: LocalDate? = null, endDate: LocalDate? = null): Flow<Summary> {
        return combine(
            lessonRepository.observeLessonDetails(),
            studentRepository.observeStudents(false),
            studentRepository.observeStudents(true)
        ) { lessons, activeStudents, archivedStudents ->
            val allExistingStudents = activeStudents + archivedStudents
            val isWithinRange = { date: LocalDate ->
                (startDate == null || !date.isBefore(startDate)) &&
                    (endDate == null || !date.isAfter(endDate))
            }

            val filteredLessons = lessons.filter { details ->
                isWithinRange(details.lesson.startTime.toLocalDate())
            }

            val paidLessons = filteredLessons.filter { details ->
                details.payment != null
            }

            val incomeTotal = paidLessons.sumOf { it.payment?.amount ?: 0.0 }

            val lessonsCount = filteredLessons.size

            val paidLessonsCount = filteredLessons.count { it.payment != null }

            val unpaidLessonsCount = filteredLessons.count { it.payment == null }

            val studentIdsFromLessons = filteredLessons.map { it.student.id }.toSet()

            val totalStudentsCount = if (startDate == null && endDate == null) {
                (allExistingStudents.map { it.id } + studentIdsFromLessons).toSet().size
            } else {
                studentIdsFromLessons.size
            }

            val now = LocalDate.now()
            val yearsToShow = if (startDate == null && endDate == null) {
                val paymentYears = paidLessons.map { it.lesson.startTime.year }
                    .distinct()
                    .sorted()
                if (paymentYears.isNotEmpty()) paymentYears else listOf(now.year)
            } else {
                val startYear =
                    startDate?.year
                        ?: paidLessons.minOfOrNull { it.lesson.startTime.year }
                        ?: now.year
                val endYear = endDate?.year ?: now.year
                (startYear..endYear).toList()
            }

            val monthlyStats = yearsToShow.flatMap { year ->
                val yearLessons = paidLessons.filter { it.lesson.startTime.year == year }
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
