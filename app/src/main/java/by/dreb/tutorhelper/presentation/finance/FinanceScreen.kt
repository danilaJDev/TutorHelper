package by.dreb.tutorhelper.presentation.finance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun FinanceScreen(viewModel: FinanceViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val unpaidCount = state.lessons.count { it.payment == null }
    val averageIncome = if (state.lessons.isNotEmpty()) {
        state.totalIncome / state.lessons.size
    } else {
        0.0
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            color = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.finance_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                FinanceSummary(
                    totalIncome = state.totalIncome,
                    paidCount = state.paidCount,
                    unpaidCount = unpaidCount,
                    averageIncome = averageIncome
                )
            }
            item {
                FinanceChart(state.lessons)
            }
            if (state.lessons.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.finance_empty),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            items(state.lessons) { lesson ->
                FinanceLessonCard(lesson)
            }
        }
    }
}

@Composable
private fun FinanceSummary(totalIncome: Double, paidCount: Int, unpaidCount: Int, averageIncome: Double) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = stringResource(R.string.finance_total_income, totalIncome))
            Text(text = stringResource(R.string.finance_paid_count, paidCount))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = {},
                    label = { Text(text = stringResource(R.string.finance_unpaid_count, unpaidCount)) },
                    leadingIcon = { androidx.compose.material3.Icon(Icons.Default.Payments, contentDescription = null) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                )
                AssistChip(
                    onClick = {},
                    label = { Text(text = stringResource(R.string.finance_average_income, averageIncome)) },
                    leadingIcon = { androidx.compose.material3.Icon(Icons.Default.Timeline, contentDescription = null) }
                )
            }
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
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                androidx.compose.material3.Icon(Icons.Default.CurrencyExchange, contentDescription = null)
                Text(
                    text = lesson.student.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Text(text = lesson.lesson.subject, style = MaterialTheme.typography.bodyMedium)
            Text(text = lesson.lesson.startTime.format(formatter), style = MaterialTheme.typography.bodyMedium)
            Text(
                text = stringResource(R.string.finance_payment_amount, lesson.payment?.amount ?: 0.0),
                style = MaterialTheme.typography.bodySmall
            )
            val statusText = if (lesson.payment == null) {
                stringResource(R.string.finance_payment_status_unpaid)
            } else {
                stringResource(R.string.finance_payment_status_paid)
            }
            Text(text = statusText, style = MaterialTheme.typography.labelMedium)
        }
    }
}
