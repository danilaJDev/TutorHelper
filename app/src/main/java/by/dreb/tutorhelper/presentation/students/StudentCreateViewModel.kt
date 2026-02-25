package by.dreb.tutorhelper.presentation.students

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.dreb.tutorhelper.domain.model.Student
import by.dreb.tutorhelper.domain.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudentCreateUiState(
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class StudentCreateViewModel @Inject constructor(
    private val studentRepository: StudentRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(StudentCreateUiState())
    val uiState = _uiState.asStateFlow()

    fun createStudent(
        name: String,
        phone: String?,
        telegramUsername: String?,
        viberPhone: String?,
        whatsappPhone: String?,
        note: String?,
        defaultPrice: Double
    ) {
        viewModelScope.launch {
            val safeName = name.trim()
            if (safeName.isBlank()) {
                _uiState.update { it.copy(errorMessage = "Имя ученика не может быть пустым") }
                return@launch
            }

            val normalizedPhone = phone?.trim()?.ifBlank { null }
            val normalizedTelegram = telegramUsername
                ?.trim()
                ?.removePrefix("@")
                ?.ifBlank { null }
            val normalizedNote = note?.trim()?.ifBlank { null }
            val normalizedPrice = defaultPrice.takeIf { it >= 0.0 } ?: 0.0

            _uiState.update { it.copy(isSaving = true, isSaved = false, errorMessage = null) }

            val student = Student(
                id = 0,
                name = safeName,
                phone = normalizedPhone,
                telegramUsername = normalizedTelegram,
                viberPhone = viberPhone?.trim()?.ifBlank { null },
                whatsappPhone = whatsappPhone?.trim()?.ifBlank { null },
                note = normalizedNote,
                isArchived = false,
                defaultPrice = normalizedPrice
            )

            runCatching { studentRepository.upsertStudent(student) }
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false, isSaved = true) }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            isSaved = false,
                            errorMessage = "Не удалось сохранить ученика"
                        )
                    }
                }
        }
    }

    fun consumeError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
