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
    private val studentId: Long = checkNotNull(savedStateHandle["studentId"])

    private val _student = MutableStateFlow<Student?>(null)
    val student: StateFlow<Student?> = _student.asStateFlow()

    init {
        viewModelScope.launch {
            _student.value = studentRepository.getStudentById(studentId)
        }
    }

    fun updateStudent(name: String, phone: String?, note: String?, defaultPrice: Double) {
        val current = _student.value ?: return
        viewModelScope.launch {
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
