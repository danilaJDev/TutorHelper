package by.dreb.tutorhelper.presentation.schedule

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.dreb.tutorhelper.domain.repository.LessonRepository
import by.dreb.tutorhelper.domain.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class LessonEditViewModel @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val studentRepository: StudentRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    val lessonId: Long = checkNotNull(savedStateHandle["lessonId"])

    val students = studentRepository.observeStudents(false)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lessonDetails = lessonRepository.observeLessonDetailsById(lessonId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun updateLesson(
        studentId: Long,
        startTime: LocalDateTime,
        durationMinutes: Int,
        price: Double,
        note: String?
    ) {
        viewModelScope.launch {
            val current = lessonDetails.value?.lesson ?: return@launch
            lessonRepository.upsertLesson(current.copy(
                studentId = studentId,
                startTime = startTime,
                durationMinutes = durationMinutes,
                price = price,
                note = note
            ))
        }
    }
}
