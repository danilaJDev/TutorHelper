package by.dreb.tutorhelper.presentation.students

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.dreb.tutorhelper.domain.model.Student
import by.dreb.tutorhelper.domain.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class StudentDetailsViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val studentId: Long = savedStateHandle.get<Long>("studentId") ?: -1L

    val student: StateFlow<Student?> = if (studentId > 0) {
        studentRepository.observeStudentById(studentId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    } else {
        kotlinx.coroutines.flow.flowOf(null)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    }
}
