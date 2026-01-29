package by.dreb.tutorhelper.presentation.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.dreb.tutorhelper.domain.usecase.GetScheduleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    getScheduleUseCase: GetScheduleUseCase
) : ViewModel() {
    private val mode = MutableStateFlow(ScheduleMode.LIST)
    private val showHidden = MutableStateFlow(false)

    val state: StateFlow<ScheduleUiState> = combine(
        getScheduleUseCase(),
        mode,
        showHidden
    ) { lessons, modeValue, hiddenValue ->
        ScheduleUiState(lessons = lessons, mode = modeValue, showHidden = hiddenValue)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ScheduleUiState())

    fun updateMode(mode: ScheduleMode) {
        this.mode.value = mode
    }

    fun updateHidden(showHidden: Boolean) {
        this.showHidden.value = showHidden
    }
}
