package by.dreb.tutorhelper.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import by.dreb.tutorhelper.data.db.entity.StudentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students WHERE isArchived = :isArchived ORDER BY name ASC")
    fun observeStudents(isArchived: Boolean): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE id = :id")
    suspend fun getStudentById(id: Long): StudentEntity?

    @Query("SELECT * FROM students WHERE id = :id")
    fun observeStudentById(id: Long): Flow<StudentEntity?>

    @Query("SELECT COUNT(*) FROM students WHERE (:isArchived IS NULL OR isArchived = :isArchived)")
    fun observeStudentsCount(isArchived: Boolean?): Flow<Int>

    @Query("SELECT COUNT(*) FROM students")
    suspend fun countStudents(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(student: StudentEntity): Long

    @Update
    suspend fun update(student: StudentEntity)

    @Query("UPDATE students SET isArchived = :archived WHERE id = :studentId")
    suspend fun updateArchived(studentId: Long, archived: Boolean)

    @Query("DELETE FROM students WHERE id = :id")
    suspend fun deleteById(id: Long)
}
