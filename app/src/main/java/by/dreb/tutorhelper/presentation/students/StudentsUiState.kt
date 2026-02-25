package by.dreb.tutorhelper.presentation.students

import by.dreb.tutorhelper.domain.model.Student

data class StudentsUiState(
    val isArchived: Boolean = false,
    val query: String = "",
    val students: List<Student> = emptyList(),
    val activeCount: Int = 0,
    val archivedCount: Int = 0
)
