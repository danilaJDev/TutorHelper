package by.dreb.tutorhelper.domain.repository

import by.dreb.tutorhelper.domain.model.Student
import kotlinx.coroutines.flow.Flow

interface StudentRepository {
    fun observeStudents(isArchived: Boolean): Flow<List<Student>>
    fun observeStudentsCount(isArchived: Boolean? = null): Flow<Int>
    suspend fun upsertStudent(student: Student)
    suspend fun archiveStudent(studentId: Long, archived: Boolean)
}
