package by.dreb.tutorhelper.data.repository

import by.dreb.tutorhelper.data.db.dao.PaymentDao
import by.dreb.tutorhelper.data.mapper.toDomain
import by.dreb.tutorhelper.data.mapper.toEntity
import by.dreb.tutorhelper.domain.model.Payment
import by.dreb.tutorhelper.domain.repository.PaymentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepositoryImpl @Inject constructor(
    private val paymentDao: PaymentDao
) : PaymentRepository {
    override fun observePayments(): Flow<List<Payment>> =
        paymentDao.observePayments().map { entities -> entities.map { it.toDomain() } }

    override suspend fun upsertPayment(payment: Payment) {
        paymentDao.upsert(payment.toEntity())
    }
}
