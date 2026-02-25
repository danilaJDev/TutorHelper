package by.dreb.tutorhelper.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import by.dreb.tutorhelper.data.db.entity.LessonEntity
import by.dreb.tutorhelper.data.db.entity.LessonWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonDao {
    @Query("SELECT * FROM lessons ORDER BY startTime ASC")
    fun observeLessons(): Flow<List<LessonEntity>>

    @Transaction
    @Query("SELECT * FROM lessons ORDER BY startTime ASC")
    fun observeLessonDetails(): Flow<List<LessonWithDetails>>

    @Transaction
    @Query("SELECT * FROM lessons WHERE startTime LIKE :datePrefix || '%' ORDER BY startTime ASC")
    fun observeLessonDetailsByDate(datePrefix: String): Flow<List<LessonWithDetails>>

    @Transaction
    @Query("SELECT * FROM lessons WHERE id = :id")
    fun observeLessonDetailsById(id: Long): Flow<LessonWithDetails?>

    @Transaction
    @Query("SELECT * FROM lessons WHERE id = :id")
    suspend fun getLessonDetailsById(id: Long): LessonWithDetails?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(lesson: LessonEntity): Long

    @Query("SELECT * FROM lessons WHERE id = :id LIMIT 1")
    suspend fun getLessonById(id: Long): LessonEntity?

    @androidx.room.Delete
    suspend fun delete(lesson: LessonEntity)

    @Query("DELETE FROM lessons WHERE studentId = :studentId")
    suspend fun deleteLessonsByStudentId(studentId: Long)

    @Query("SELECT * FROM lessons WHERE studentId = :studentId AND startTime >= :startTime ORDER BY startTime ASC")
    suspend fun getLessonsByStudentFrom(studentId: Long, startTime: String): List<LessonEntity>

    @Transaction
    @Query("SELECT * FROM lessons WHERE studentId = :studentId AND startTime > :startTime ORDER BY startTime ASC")
    suspend fun getLessonDetailsByStudentAfter(studentId: Long, startTime: String): List<LessonWithDetails>
}
