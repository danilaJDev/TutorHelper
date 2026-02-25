package by.dreb.tutorhelper.domain.repository

import by.dreb.tutorhelper.domain.model.Payment
import kotlinx.coroutines.flow.Flow

interface PaymentRepository {
    fun observePayments(): Flow<List<Payment>>
    fun observeTotalIncome(): Flow<Double>
    fun observePaymentsCount(): Flow<Int>
    suspend fun upsertPayment(payment: Payment)
    suspend fun deletePaymentByLessonId(lessonId: Long)
}
