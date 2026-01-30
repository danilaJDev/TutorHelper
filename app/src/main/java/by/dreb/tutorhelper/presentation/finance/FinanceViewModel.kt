package by.dreb.tutorhelper.presentation.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.dreb.tutorhelper.domain.repository.PaymentRepository
import by.dreb.tutorhelper.domain.usecase.GetFinanceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class FinanceViewModel @Inject constructor(
    getFinanceUseCase: GetFinanceUseCase,
    paymentRepository: PaymentRepository
) : ViewModel() {
    val state: StateFlow<FinanceUiState> = combine(
        getFinanceUseCase(),
        paymentRepository.observeTotalIncome(),
        paymentRepository.observePaymentsCount()
    ) { lessons, totalIncome, paidCount ->
        FinanceUiState(
            lessons = lessons,
            totalIncome = totalIncome,
            paidCount = paidCount
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FinanceUiState())
}
