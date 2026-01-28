package by.dreb.tutorhelper.presentation.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.dreb.tutorhelper.domain.usecase.GetFinanceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class FinanceViewModel @Inject constructor(
    getFinanceUseCase: GetFinanceUseCase
) : ViewModel() {
    val state: StateFlow<FinanceUiState> = getFinanceUseCase().map { lessons ->
        FinanceUiState(
            lessons = lessons,
            totalIncome = lessons.sumOf { it.payment?.amount ?: 0.0 },
            paidCount = lessons.count { it.payment != null }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FinanceUiState())
}
