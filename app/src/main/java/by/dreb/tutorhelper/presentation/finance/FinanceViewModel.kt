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
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class FinanceViewModel @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val _filter = MutableStateFlow(FinanceFilter.UNPAID)
    private val _expandedStudentIds = MutableStateFlow(setOf<Long>())

    val state: StateFlow<FinanceUiState> = combine(
        lessonRepository.observeLessonDetails(),
        _filter,
        _expandedStudentIds
    ) { lessons, filter, expandedIds ->
        val now = LocalDateTime.now()
        val processedLessons = lessons.map { details ->
            val endTime = details.lesson.startTime.plusMinutes(details.lesson.durationMinutes.toLong())
            if (now.isAfter(endTime) && !details.lesson.isCompleted) {
                details.copy(lesson = details.lesson.copy(isCompleted = true))
            } else {
                details
            }
        }

        val filteredLessons = processedLessons.filter { details ->
            if (details.lesson.isHidden) return@filter false

            when (filter) {
                FinanceFilter.UNPAID -> details.payment == null
                FinanceFilter.PAID -> details.payment != null
            }
        }

        val grouped = filteredLessons
            .groupBy { it.student }
            .toSortedMap(compareBy { it.name })

        val listItems = mutableListOf<FinanceListItem>()
        grouped.forEach { (student, studentLessons) ->
            val isExpanded = expandedIds.contains(student.id)
            listItems.add(FinanceListItem.StudentHeader(student, isExpanded))
            if (isExpanded) {
                studentLessons.forEach { lesson ->
                    listItems.add(FinanceListItem.LessonItem(lesson))
                }
            }
        }

        FinanceUiState(
            filter = filter,
            listItems = listItems
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

    fun markAsPaid(details: LessonDetails) {
        viewModelScope.launch {
            val payment = Payment(
                id = 0,
                lessonId = details.lesson.id,
                amount = details.lesson.price,
                paidOn = LocalDate.now(),
                method = null
            )
            paymentRepository.upsertPayment(payment)
        }
    }

    fun toggleHidden(details: LessonDetails) {
        viewModelScope.launch {
            lessonRepository.upsertLesson(details.lesson.copy(isHidden = true))
        }
    }

    fun deleteLesson(details: LessonDetails) {
        viewModelScope.launch {
            lessonRepository.deleteLesson(details.lesson)
        }
    }
}
