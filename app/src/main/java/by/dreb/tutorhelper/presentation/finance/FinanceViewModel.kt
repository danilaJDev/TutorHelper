package by.dreb.tutorhelper.presentation.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.dreb.tutorhelper.domain.model.LessonDetails
import by.dreb.tutorhelper.domain.model.Payment
import by.dreb.tutorhelper.domain.repository.LessonRepository
import by.dreb.tutorhelper.domain.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class FinanceViewModel @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val _filter = MutableStateFlow(FinanceFilter.ACTIVE)
    private val _expandedStudentIds = MutableStateFlow(setOf<Long>())

    val state: StateFlow<FinanceUiState> = combine(
        lessonRepository.observeLessonDetails(),
        _filter,
        _expandedStudentIds
    ) { lessons, filter, expandedIds ->
        val filteredLessons = lessons.filter { details ->
            val isPaid = details.payment != null
            val isArchived = details.lesson.isCompleted && details.lesson.isHomeworkSent && isPaid

            when (filter) {
                FinanceFilter.ACTIVE -> !isArchived
                FinanceFilter.ARCHIVED -> isArchived
            }
        }

        val grouped = filteredLessons
            .groupBy { it.student }
            .toSortedMap(compareBy<by.dreb.tutorhelper.domain.model.Student> { it.name }.thenBy { it.id })

        val sections = grouped.map { (student, studentLessons) ->
            val sortedLessons = studentLessons.sortedByDescending { it.lesson.startTime }
            val totalAmount = sortedLessons.sumOf { it.lesson.price }
            val paidAmount = sortedLessons.sumOf { it.payment?.amount ?: 0.0 }
            val unpaidAmount = totalAmount - paidAmount
            FinanceStudentSection(
                student = student,
                lessons = sortedLessons,
                isExpanded = expandedIds.contains(student.id),
                totalAmount = totalAmount,
                paidAmount = paidAmount,
                unpaidAmount = unpaidAmount
            )
        }

        FinanceUiState(
            filter = filter,
            sections = sections
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FinanceUiState())

    fun updateFilter(filter: FinanceFilter) {
        _filter.value = filter
    }

    fun toggleStudentExpanded(studentId: Long) {
        _expandedStudentIds.update { current ->
            if (current.contains(studentId)) {
                current - studentId
            } else {
                current + studentId
            }
        }
    }

    fun togglePayment(details: LessonDetails) {
        viewModelScope.launch {
            if (details.payment == null) {
                val payment = Payment(
                    id = 0,
                    lessonId = details.lesson.id,
                    studentId = details.student.id,
                    amount = details.lesson.price,
                    paidOn = LocalDate.now(),
                    method = null
                )
                paymentRepository.upsertPayment(payment)
            } else {
                paymentRepository.deletePaymentByLessonId(details.lesson.id)
            }
        }
    }
}
