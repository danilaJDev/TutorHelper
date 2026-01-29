package by.dreb.tutorhelper.presentation.schedule

import by.dreb.tutorhelper.domain.model.LessonDetails
import java.time.LocalDate

data class ScheduleUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val lessons: List<LessonDetails> = emptyList(),
    val mode: ScheduleMode = ScheduleMode.LIST,
    val showHidden: Boolean = false
)

enum class ScheduleMode {
    LIST,
    TABLE,
    CALENDAR
}
