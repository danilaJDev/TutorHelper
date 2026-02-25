package by.dreb.tutorhelper.presentation.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.net.Uri
import by.dreb.tutorhelper.domain.backup.BackupRepository
import by.dreb.tutorhelper.domain.usecase.GetSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SummaryViewModel @Inject constructor(
    private val getSummaryUseCase: GetSummaryUseCase,
    private val backupRepository: BackupRepository
) : ViewModel() {
    private val _startDate = MutableStateFlow<LocalDate?>(null)
    private val _endDate = MutableStateFlow<LocalDate?>(null)

    val state: StateFlow<SummaryUiState> = combine(_startDate, _endDate) { start, end ->
        start to end
    }.flatMapLatest { (start, end) ->
        getSummaryUseCase(start, end).map { summary ->
            SummaryUiState(summary, start, end)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SummaryUiState())

    fun updatePeriod(start: LocalDate?, end: LocalDate?) {
        _startDate.value = start
        _endDate.value = end
    }

    suspend fun exportBackup(uri: Uri) {
        backupRepository.exportToUri(uri)
    }

    suspend fun importBackup(uri: Uri) {
        backupRepository.importFromUri(uri)
    }
}
