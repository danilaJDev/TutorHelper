package by.dreb.tutorhelper.data.repository

import by.dreb.tutorhelper.data.db.dao.LessonDao
import by.dreb.tutorhelper.data.mapper.toDomain
import by.dreb.tutorhelper.data.mapper.toEntity
import by.dreb.tutorhelper.domain.model.Lesson
import by.dreb.tutorhelper.domain.model.LessonDetails
import by.dreb.tutorhelper.domain.repository.LessonRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LessonRepositoryImpl @Inject constructor(
    private val lessonDao: LessonDao
) : LessonRepository {
    override fun observeLessons(): Flow<List<Lesson>> =
        lessonDao.observeLessons().map { entities -> entities.map { it.toDomain() } }

    override fun observeLessonDetails(): Flow<List<LessonDetails>> =
        lessonDao.observeLessonDetails().map { entities -> entities.map { it.toDomain() } }

    override fun observeLessonsByDate(date: LocalDate): Flow<List<LessonDetails>> =
        lessonDao.observeLessonDetailsByDate(date.toString()).map { entities -> entities.map { it.toDomain() } }

    override suspend fun upsertLesson(lesson: Lesson) {
        lessonDao.upsert(lesson.toEntity())
    }

    override suspend fun deleteLesson(lesson: Lesson) {
        lessonDao.delete(lesson.toEntity())
    }
}
