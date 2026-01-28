package by.dreb.tutorhelper.presentation.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.dreb.tutorhelper.domain.usecase.GetSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SummaryViewModel @Inject constructor(
    getSummaryUseCase: GetSummaryUseCase
) : ViewModel() {
    val state: StateFlow<SummaryUiState> = getSummaryUseCase().map { SummaryUiState(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SummaryUiState())
}
