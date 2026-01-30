package by.dreb.tutorhelper.presentation.finance

import by.dreb.tutorhelper.domain.model.LessonDetails
import by.dreb.tutorhelper.domain.model.Student

data class FinanceUiState(
    val filter: FinanceFilter = FinanceFilter.UNPAID,
    val listItems: List<FinanceListItem> = emptyList()
)

enum class FinanceFilter {
    UNPAID, PAID
}

sealed class FinanceListItem {
    data class StudentHeader(val student: Student, val isExpanded: Boolean) : FinanceListItem()
    data class LessonItem(val details: LessonDetails) : FinanceListItem()
}
