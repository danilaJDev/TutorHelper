package by.dreb.tutorhelper.presentation.finance

import by.dreb.tutorhelper.domain.model.LessonDetails

data class FinanceUiState(
    val lessons: List<LessonDetails> = emptyList(),
    val totalIncome: Double = 0.0,
    val paidCount: Int = 0
)
