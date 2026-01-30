package by.dreb.tutorhelper.presentation.summary

import by.dreb.tutorhelper.domain.model.Summary
import java.time.LocalDate

data class SummaryUiState(
    val summary: Summary = Summary(0.0, 0, 0, 0, 0, emptyList()),
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null
)
