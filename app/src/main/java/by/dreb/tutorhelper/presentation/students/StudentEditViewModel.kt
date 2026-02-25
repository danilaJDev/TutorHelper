package by.dreb.tutorhelper.presentation.students

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.dreb.tutorhelper.R
import by.dreb.tutorhelper.domain.model.Student
import by.dreb.tutorhelper.domain.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentEditViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val studentId: Long = savedStateHandle.get<Long>("studentId") ?: -1L

    private val _student = MutableStateFlow<Student?>(null)
    val student: StateFlow<Student?> = _student.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved = _isSaved.asStateFlow()

    private val _nameError = MutableStateFlow<Int?>(null)
    val nameError = _nameError.asStateFlow()

    init {
        if (studentId > 0) {
            viewModelScope.launch {
                _student.value = runCatching {
                    studentRepository.getStudentById(studentId)
                }.getOrNull()
            }
        }
    }

    fun updateStudent(name: String, phone: String?, note: String?, defaultPrice: Double) {
        val current = _student.value ?: return
        val safeName = name.trim()
        if (safeName.isBlank()) {
            _nameError.value = R.string.error_name_empty
            return
        }
        _nameError.value = null

        viewModelScope.launch {
            val result = runCatching {
                studentRepository.upsertStudent(
                    current.copy(
                        name = safeName,
                        phone = phone,
                        note = note,
                        defaultPrice = defaultPrice
                    )
                )
            }
            if (result.isSuccess) {
                _isSaved.value = true
            }
        }
    }

    fun clearErrors() {
        _nameError.value = null
    }
}
