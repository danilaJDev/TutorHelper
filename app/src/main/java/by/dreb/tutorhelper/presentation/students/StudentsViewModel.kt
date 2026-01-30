package by.dreb.tutorhelper.presentation.students

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.dreb.tutorhelper.domain.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentsViewModel @Inject constructor(
    private val studentRepository: StudentRepository
) : ViewModel() {
    private val isArchived = MutableStateFlow(false)
    private val query = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<StudentsUiState> = combine(
        isArchived,
        query,
        isArchived.flatMapLatest { archived -> studentRepository.observeStudents(archived) },
        studentRepository.observeStudentsCount(false),
        studentRepository.observeStudentsCount(true)
    ) { archived, queryValue, students, activeCount, archivedCount ->
        val filtered = if (queryValue.isBlank()) {
            students
        } else {
            students.filter { it.name.contains(queryValue, ignoreCase = true) }
        }
        StudentsUiState(
            isArchived = archived,
            query = queryValue,
            students = filtered,
            activeCount = activeCount,
            archivedCount = archivedCount
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StudentsUiState())

    fun toggleArchive(showArchived: Boolean) {
        isArchived.value = showArchived
    }

    fun updateQuery(value: String) {
        query.value = value
    }

    fun setStudentArchived(studentId: Long, archived: Boolean) {
        viewModelScope.launch {
            studentRepository.archiveStudent(studentId, archived)
        }
    }
}
