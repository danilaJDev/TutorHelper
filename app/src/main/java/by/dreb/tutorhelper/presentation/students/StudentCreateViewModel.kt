package by.dreb.tutorhelper.presentation.students

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.dreb.tutorhelper.domain.model.Student
import by.dreb.tutorhelper.domain.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentCreateViewModel @Inject constructor(
    private val studentRepository: StudentRepository
) : ViewModel() {
    fun createStudent(name: String, phone: String?, note: String?, defaultPrice: Double) {
        viewModelScope.launch {
            val student = Student(
                id = 0,
                name = name,
                phone = phone,
                note = note,
                isArchived = false,
                defaultPrice = defaultPrice
            )
            studentRepository.upsertStudent(student)
        }
    }
}
