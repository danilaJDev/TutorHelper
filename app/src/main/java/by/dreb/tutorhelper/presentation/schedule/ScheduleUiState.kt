package by.dreb.tutorhelper.presentation.schedule

import by.dreb.tutorhelper.domain.model.LessonDetails
import java.time.LocalDate

data class ScheduleUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val lessons: List<LessonDetails> = emptyList(),
    val mode: ScheduleMode = ScheduleMode.LIST
)

enum class ScheduleMode {
    LIST,
    TABLE,
    CALENDAR
}
