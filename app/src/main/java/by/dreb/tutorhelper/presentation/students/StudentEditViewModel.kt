package by.dreb.tutorhelper.presentation.students

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.dreb.tutorhelper.domain.model.Student
import by.dreb.tutorhelper.domain.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudentEditUiState(
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class StudentEditViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val studentId: Long = savedStateHandle.get<Long>("studentId") ?: -1L

    private val _student = MutableStateFlow<Student?>(null)
    val student: StateFlow<Student?> = _student.asStateFlow()

    private val _uiState = MutableStateFlow(StudentEditUiState())
    val uiState: StateFlow<StudentEditUiState> = _uiState.asStateFlow()

    init {
        if (studentId > 0) {
            viewModelScope.launch {
                _student.value = runCatching {
                    studentRepository.getStudentById(studentId)
                }.getOrNull()
            }
        }
    }

    fun updateStudent(
        name: String,
        phone: String?,
        telegramUsername: String?,
        viberPhone: String?,
        whatsappPhone: String?,
        note: String?,
        defaultPrice: Double
    ) {
        val current = _student.value ?: return
        viewModelScope.launch {
            val safeName = name.trim()
            if (safeName.isBlank()) {
                _uiState.update { it.copy(errorMessage = "Имя ученика не может быть пустым") }
                return@launch
            }

            _uiState.update { it.copy(isSaving = true, isSaved = false, errorMessage = null) }

            runCatching {
                studentRepository.upsertStudent(
                    current.copy(
                        name = safeName,
                        phone = phone?.trim()?.ifBlank { null },
                        telegramUsername = telegramUsername
                            ?.trim()
                            ?.removePrefix("@")
                            ?.ifBlank { null },
                        viberPhone = viberPhone?.trim()?.ifBlank { null },
                        whatsappPhone = whatsappPhone?.trim()?.ifBlank { null },
                        note = note?.trim()?.ifBlank { null },
                        defaultPrice = defaultPrice.takeIf { it >= 0.0 } ?: 0.0
                    )
                )
            }
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false, isSaved = true) }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            isSaved = false,
                            errorMessage = "Не удалось сохранить изменения"
                        )
                    }
                }
        }
    }

    fun consumeError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
