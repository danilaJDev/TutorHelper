package by.dreb.tutorhelper.presentation.finance

import by.dreb.tutorhelper.domain.model.LessonDetails
import by.dreb.tutorhelper.domain.model.Student

data class FinanceUiState(
    val filter: FinanceFilter = FinanceFilter.ACTIVE,
    val sections: List<FinanceStudentSection> = emptyList()
)

enum class FinanceFilter {
    ACTIVE, ARCHIVED
}

data class FinanceStudentSection(
    val student: Student,
    val lessons: List<LessonDetails>,
    val isExpanded: Boolean,
    val totalAmount: Int,
    val paidAmount: Int,
    val unpaidAmount: Int
)
