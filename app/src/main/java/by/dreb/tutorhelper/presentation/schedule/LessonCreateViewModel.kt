package by.dreb.tutorhelper.presentation.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.dreb.tutorhelper.domain.model.Lesson
import by.dreb.tutorhelper.domain.repository.LessonRepository
import by.dreb.tutorhelper.domain.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class LessonCreateViewModel @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val studentRepository: StudentRepository
) : ViewModel() {
    val students = studentRepository.observeStudents(false)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createLesson(
        studentId: Long,
        startTime: LocalDateTime,
        durationMinutes: Int,
        price: Double,
        note: String?,
        isDuplicate: Boolean,
        duplicateUntil: LocalDate?
    ) {
        viewModelScope.launch {
            val baseLesson = Lesson(
                id = 0,
                studentId = studentId,
                subject = "Занятие", // Default subject
                startTime = startTime,
                durationMinutes = durationMinutes,
                price = price,
                note = note
            )
            lessonRepository.upsertLesson(baseLesson)

            if (isDuplicate && duplicateUntil != null) {
                var currentStartTime = startTime.plusWeeks(1)
                while (!currentStartTime.toLocalDate().isAfter(duplicateUntil)) {
                    lessonRepository.upsertLesson(baseLesson.copy(startTime = currentStartTime))
                    currentStartTime = currentStartTime.plusWeeks(1)
                }
            }
        }
    }
}
