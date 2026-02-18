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
        viewModelScope.launch {
            runCatching {
                studentRepository.upsertStudent(
                current.copy(
                    name = name,
                    phone = phone,
                    note = note,
                    defaultPrice = defaultPrice
                )
            )
            }
        }
    }
}
