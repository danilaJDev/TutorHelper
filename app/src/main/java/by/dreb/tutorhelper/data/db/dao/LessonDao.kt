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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(lesson: LessonEntity): Long

    @androidx.room.Delete
    suspend fun delete(lesson: LessonEntity)
}
