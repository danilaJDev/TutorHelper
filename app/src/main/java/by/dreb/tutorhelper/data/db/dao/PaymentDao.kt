package by.dreb.tutorhelper.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import by.dreb.tutorhelper.data.db.entity.PaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY paidOn DESC")
    fun observePayments(): Flow<List<PaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(payment: PaymentEntity): Long

    @Query("SELECT SUM(amount) FROM payments")
    fun observeTotalIncome(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM payments")
    fun observePaymentsCount(): Flow<Int>

    @Query("SELECT * FROM payments WHERE lessonId = :lessonId LIMIT 1")
    suspend fun getByLessonId(lessonId: Long): PaymentEntity?

    @Query("DELETE FROM payments WHERE lessonId = :lessonId")
    suspend fun deleteByLessonId(lessonId: Long)

    @Query("DELETE FROM payments WHERE lessonId IN (SELECT id FROM lessons WHERE studentId = :studentId)")
    suspend fun deleteByStudentId(studentId: Long)
}
