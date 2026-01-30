package by.dreb.tutorhelper.data.repository

import by.dreb.tutorhelper.data.db.dao.LessonDao
import by.dreb.tutorhelper.data.db.dao.StudentDao
import by.dreb.tutorhelper.data.mapper.toDomain
import by.dreb.tutorhelper.data.mapper.toEntity
import by.dreb.tutorhelper.domain.model.Student
import by.dreb.tutorhelper.domain.repository.StudentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StudentRepositoryImpl @Inject constructor(
    private val studentDao: StudentDao,
    private val lessonDao: LessonDao
) : StudentRepository {
    override fun observeStudents(isArchived: Boolean): Flow<List<Student>> =
        studentDao.observeStudents(isArchived).map { entities -> entities.map { it.toDomain() } }

    override fun observeStudentsCount(isArchived: Boolean?): Flow<Int> =
        studentDao.observeStudentsCount(isArchived)

    override suspend fun getStudentById(id: Long): Student? =
        studentDao.getStudentById(id)?.toDomain()

    override fun observeStudentById(id: Long): Flow<Student?> =
        studentDao.observeStudentById(id).map { it?.toDomain() }

    override suspend fun upsertStudent(student: Student) {
        studentDao.upsert(student.toEntity())
    }

    override suspend fun archiveStudent(studentId: Long, archived: Boolean) {
        studentDao.updateArchived(studentId, archived)
    }

    override suspend fun deleteStudentFull(studentId: Long) {
        lessonDao.deleteLessonsByStudentId(studentId)
        studentDao.deleteById(studentId)
    }
}
