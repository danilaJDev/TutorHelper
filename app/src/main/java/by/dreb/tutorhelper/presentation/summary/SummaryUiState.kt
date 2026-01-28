package by.dreb.tutorhelper.presentation.summary

import by.dreb.tutorhelper.domain.model.Summary

data class SummaryUiState(
    val summary: Summary = Summary(0, 0, 0, 0, 0.0)
)
