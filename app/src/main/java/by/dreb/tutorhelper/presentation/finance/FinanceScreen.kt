package by.dreb.tutorhelper.presentation.finance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import by.dreb.tutorhelper.R
import by.dreb.tutorhelper.domain.model.LessonDetails
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.time.format.DateTimeFormatter

@Composable
fun FinanceScreen(viewModel: FinanceViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.finance_title),
                style = MaterialTheme.typography.headlineSmall
            )
        }
        item {
            FinanceSummary(totalIncome = state.totalIncome, paidCount = state.paidCount)
        }
        item {
            FinanceChart(state.lessons)
        }
        items(state.lessons) { lesson ->
            FinanceLessonCard(lesson)
        }
    }
}

@Composable
private fun FinanceSummary(totalIncome: Double, paidCount: Int) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = stringResource(R.string.finance_total_income, totalIncome))
            Text(text = stringResource(R.string.finance_paid_count, paidCount))
        }
    }
}

@Composable
private fun FinanceChart(lessons: List<LessonDetails>) {
    val context = LocalContext.current
    val entries = lessons.take(6).mapIndexed { index, lesson ->
        BarEntry(index.toFloat(), (lesson.payment?.amount ?: 0.0).toFloat())
    }
    val labels = lessons.take(6).map { it.lesson.startTime.toLocalDate().dayOfMonth.toString() }

    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = stringResource(R.string.finance_chart_title), style = MaterialTheme.typography.titleMedium)
            AndroidView(
                factory = {
                    BarChart(context).apply {
                        description.isEnabled = false
                        setFitBars(true)
                        axisRight.isEnabled = false
                        xAxis.position = XAxis.XAxisPosition.BOTTOM
                        xAxis.granularity = 1f
                        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                    }
                },
                update = { chart ->
                    val dataSet = BarDataSet(entries, context.getString(R.string.finance_chart_label)).apply {
                        color = chart.context.getColor(android.R.color.holo_blue_light)
                    }
                    chart.data = BarData(dataSet)
                    chart.invalidate()
                }
            )
        }
    }
}

@Composable
private fun FinanceLessonCard(lesson: LessonDetails) {
    val formatter = DateTimeFormatter.ofPattern("dd MMM, HH:mm")
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = lesson.student.name, style = MaterialTheme.typography.titleMedium)
            Text(text = lesson.lesson.subject, style = MaterialTheme.typography.bodyMedium)
            Text(text = lesson.lesson.startTime.format(formatter), style = MaterialTheme.typography.bodyMedium)
            Text(
                text = stringResource(R.string.finance_payment_amount, lesson.payment?.amount ?: 0.0),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
