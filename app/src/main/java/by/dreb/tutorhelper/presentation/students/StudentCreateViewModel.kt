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
            if (safeName.isBlank()) return@launch
            val student = Student(
                id = 0,
                name = safeName,
                phone = phone,
                telegramUsername = telegramUsername,
                viberPhone = viberPhone,
                whatsappPhone = whatsappPhone,
                note = note,
                isArchived = false,
                defaultPrice = defaultPrice
            )
            runCatching { studentRepository.upsertStudent(student) }
        }
    }
}
