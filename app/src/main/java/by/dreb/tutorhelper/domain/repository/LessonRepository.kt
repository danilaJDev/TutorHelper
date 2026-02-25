package by.dreb.tutorhelper.domain.repository

import by.dreb.tutorhelper.domain.model.Lesson
import by.dreb.tutorhelper.domain.model.LessonDetails
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface LessonRepository {
    fun observeLessons(): Flow<List<Lesson>>
    fun observeLessonDetails(): Flow<List<LessonDetails>>
    fun observeLessonsByDate(date: LocalDate): Flow<List<LessonDetails>>
    fun observeLessonDetailsById(id: Long): Flow<LessonDetails?>
    suspend fun upsertLesson(lesson: Lesson)
    suspend fun deleteLesson(lesson: Lesson)
    suspend fun deleteLessonWithFutureDuplicates(lesson: Lesson)
}
