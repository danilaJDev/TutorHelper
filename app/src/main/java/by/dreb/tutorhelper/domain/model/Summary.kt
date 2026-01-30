package by.dreb.tutorhelper.domain.model

data class Summary(
    val incomeTotal: Double,
    val lessonsCount: Int,
    val studentsCount: Int,
    val paidLessonsCount: Int,
    val unpaidLessonsCount: Int,
    val monthlyStats: List<MonthlyStat>
)

data class MonthlyStat(
    val year: Int,
    val month: Int,
    val income: Double,
    val maxMonthlyIncome: Double
)
