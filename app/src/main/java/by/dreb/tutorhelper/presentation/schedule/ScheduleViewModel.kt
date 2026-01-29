package by.dreb.tutorhelper.presentation.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.dreb.tutorhelper.domain.model.LessonDetails
import by.dreb.tutorhelper.domain.repository.LessonRepository
import by.dreb.tutorhelper.domain.usecase.GetScheduleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    getScheduleUseCase: GetScheduleUseCase,
    private val lessonRepository: LessonRepository
) : ViewModel() {
    private val mode = MutableStateFlow(ScheduleMode.LIST)
    private val filter = MutableStateFlow(ScheduleFilter.ACTIVE)
    private val selectedDate = MutableStateFlow(LocalDate.now())

    val state: StateFlow<ScheduleUiState> = combine(
        getScheduleUseCase(),
        mode,
        filter,
        selectedDate
    ) { lessons, modeValue, filterValue, dateValue ->
        val filteredLessons = lessons.filter {
            when (filterValue) {
                ScheduleFilter.ACTIVE -> !it.lesson.isHidden
                ScheduleFilter.HIDDEN -> it.lesson.isHidden
            }
        }
        ScheduleUiState(
            lessons = filteredLessons,
            mode = modeValue,
            filter = filterValue,
            selectedDate = dateValue
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ScheduleUiState())

    fun updateMode(mode: ScheduleMode) {
        this.mode.value = mode
    }

    fun updateFilter(filter: ScheduleFilter) {
        this.filter.value = filter
    }

    fun updateSelectedDate(date: LocalDate) {
        this.selectedDate.value = date
    }

    fun toggleHidden(lesson: LessonDetails) {
        viewModelScope.launch {
            lessonRepository.upsertLesson(lesson.lesson.copy(isHidden = !lesson.lesson.isHidden))
        }
    }

    fun toggleHomework(lesson: LessonDetails) {
        viewModelScope.launch {
            lessonRepository.upsertLesson(lesson.lesson.copy(isHomeworkSent = !lesson.lesson.isHomeworkSent))
        }
    }

    fun toggleCompleted(lesson: LessonDetails) {
        viewModelScope.launch {
            lessonRepository.upsertLesson(lesson.lesson.copy(isCompleted = !lesson.lesson.isCompleted))
        }
    }
}
