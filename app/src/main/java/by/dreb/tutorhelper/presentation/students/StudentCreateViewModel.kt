package by.dreb.tutorhelper.presentation.students

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.dreb.tutorhelper.R
import by.dreb.tutorhelper.domain.model.Student
import by.dreb.tutorhelper.domain.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentCreateViewModel @Inject constructor(
    private val studentRepository: StudentRepository
) : ViewModel() {
    private val _isSaved = MutableStateFlow(false)
    val isSaved = _isSaved.asStateFlow()

    private val _nameError = MutableStateFlow<Int?>(null)
    val nameError = _nameError.asStateFlow()

    fun createStudent(name: String, phone: String?, note: String?, defaultPrice: Double) {
        val safeName = name.trim()
        if (safeName.isBlank()) {
            _nameError.value = R.string.error_name_empty
            return
        }
        _nameError.value = null

        viewModelScope.launch {
            val student = Student(
                id = 0,
                name = safeName,
                phone = phone,
                note = note,
                isArchived = false,
                defaultPrice = defaultPrice
            )
            val result = runCatching { studentRepository.upsertStudent(student) }
            if (result.isSuccess) {
                _isSaved.value = true
            }
        }
    }

    fun clearErrors() {
        _nameError.value = null
    }
}
