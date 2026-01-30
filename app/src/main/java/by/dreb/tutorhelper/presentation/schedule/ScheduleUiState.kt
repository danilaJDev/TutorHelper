package by.dreb.tutorhelper.presentation.schedule

import by.dreb.tutorhelper.domain.model.LessonDetails
import java.time.LocalDate

data class ScheduleUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val lessons: List<LessonDetails> = emptyList(),
    val calendarLessonsByDate: Map<LocalDate, List<LessonDetails>> = emptyMap(),
    val mode: ScheduleMode = ScheduleMode.LIST,
    val filter: ScheduleFilter = ScheduleFilter.ACTIVE
)

enum class ScheduleFilter {
    ACTIVE,
    HIDDEN
}

enum class ScheduleMode {
    LIST,
    CALENDAR
}
