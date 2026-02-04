package by.dreb.tutorhelper.presentation.summary

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import by.dreb.tutorhelper.R
import by.dreb.tutorhelper.domain.model.MonthlyStat
import by.dreb.tutorhelper.domain.model.Summary
import by.dreb.tutorhelper.presentation.common.AppPalette
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    viewModel: SummaryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.summary_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = AppPalette.TextPrimary
                    )
                },
                windowInsets = WindowInsets(top = 0.dp),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppPalette.Background,
                    scrolledContainerColor = AppPalette.Surface.copy(alpha = 0.95f)
                )
            )
        },
        containerColor = AppPalette.Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            PeriodFilterBar(
                startDate = state.startDate,
                endDate = state.endDate,
                onClick = { showDatePicker = true },
                onClear = { viewModel.updatePeriod(null, null) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item { SummaryHeroCard(state.summary) }
                item { SummaryStatsGrid(state.summary) }
                item { MonthlyIncomeSection(state.summary.monthlyStats) }
            }
        }
    }

    if (showDatePicker) {
        PeriodRangePickerDialog(
            onDismiss = { showDatePicker = false },
            onDateRangeSelected = { start, end ->
                viewModel.updatePeriod(start, end)
                showDatePicker = false
            }
        )
    }
}

@Composable
private fun PeriodFilterBar(
    startDate: LocalDate?,
    endDate: LocalDate?,
    onClick: () -> Unit,
    onClear: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = AppPalette.Surface,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = AppPalette.Primary
            )
            Spacer(modifier = Modifier.size(12.dp))
            Text(
                text = if (startDate != null && endDate != null) {
                    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                    "${startDate.format(formatter)} - ${endDate.format(formatter)}"
                } else {
                    stringResource(R.string.summary_period_all_time)
                },
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
                color = AppPalette.TextPrimary
            )
            if (startDate != null) {
                IconButton(onClick = onClear, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = AppPalette.TextSecondary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeriodRangePickerDialog(
    onDismiss: () -> Unit,
    onDateRangeSelected: (LocalDate, LocalDate) -> Unit
) {
    val dateRangePickerState = rememberDateRangePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val startMillis = dateRangePickerState.selectedStartDateMillis
                    val endMillis = dateRangePickerState.selectedEndDateMillis
                    if (startMillis != null && endMillis != null) {
                        val start = Instant.ofEpochMilli(startMillis).atZone(ZoneId.systemDefault()).toLocalDate()
                        val end = Instant.ofEpochMilli(endMillis).atZone(ZoneId.systemDefault()).toLocalDate()
                        onDateRangeSelected(start, end)
                    }
                },
                enabled = dateRangePickerState.selectedEndDateMillis != null
            ) {
                Text(stringResource(R.string.action_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    ) {
        DateRangePicker(
            state = dateRangePickerState,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SummaryHeroCard(summary: Summary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppPalette.Primary.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.summary_income),
                style = MaterialTheme.typography.titleMedium,
                color = AppPalette.TextSecondary
            )
            Text(
                text = String.format("%.2f %s", summary.incomeTotal, stringResource(R.string.currency_rub)),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = AppPalette.TextPrimary
            )
        }
    }
}

@Composable
private fun SummaryStatsGrid(summary: Summary) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryStatCard(
                label = stringResource(R.string.summary_total_lessons),
                value = summary.lessonsCount.toString(),
                icon = Icons.Default.School,
                modifier = Modifier.weight(1f)
            )
            SummaryStatCard(
                label = stringResource(R.string.summary_total_students),
                value = summary.studentsCount.toString(),
                icon = Icons.Default.Groups,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryStatCard(
                label = stringResource(R.string.summary_paid_short),
                value = summary.paidLessonsCount.toString(),
                icon = Icons.Default.Payments,
                modifier = Modifier.weight(1f),
                color = AppPalette.Success.copy(alpha = 0.2f)
            )
            SummaryStatCard(
                label = stringResource(R.string.summary_unpaid),
                value = summary.unpaidLessonsCount.toString(),
                icon = Icons.Default.Payments,
                modifier = Modifier.weight(1f),
                color = AppPalette.Action.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
private fun SummaryStatCard(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    color: Color = AppPalette.Surface
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = AppPalette.TextSecondary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = AppPalette.TextPrimary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = AppPalette.TextSecondary
            )
        }
    }
}

@Composable
private fun MonthlyIncomeSection(monthlyStats: List<MonthlyStat>) {
    if (monthlyStats.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = stringResource(R.string.summary_monthly_income),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = AppPalette.TextPrimary
        )

        val statsByYear = monthlyStats.groupBy { it.year }.toSortedMap(reverseOrder())
        statsByYear.forEach { (year, stats) ->
            YearlyIncomeCard(year, stats)
        }
    }
}

@Composable
private fun YearlyIncomeCard(year: Int, stats: List<MonthlyStat>) {
    val totalYearIncome = stats.sumOf { it.income }
    val russianLocale = Locale("ru")
    val monthNames = (1..12).map {
        LocalDate.of(year, it, 1).format(DateTimeFormatter.ofPattern("LLL", russianLocale)).uppercase()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppPalette.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = year.toString(),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = AppPalette.TextPrimary
                )
                Text(
                    text = String.format("%.0f %s", totalYearIncome, stringResource(R.string.currency_rub)),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = AppPalette.Primary
                    )
                )
            }

            stats.sortedBy { it.month }.forEach { stat ->
                MonthIncomeRow(monthNames[stat.month - 1], stat.income, stat.maxMonthlyIncome)
            }
        }
    }
}

@Composable
private fun MonthIncomeRow(month: String, income: Double, maxIncome: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = month,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.width(60.dp),
            color = AppPalette.TextPrimary
        )

        Box(modifier = Modifier.weight(1f).height(24.dp), contentAlignment = Alignment.CenterStart) {
            Surface(
                modifier = Modifier.fillMaxWidth(fraction = (income / maxIncome).toFloat().coerceIn(0.01f, 1f)),
                shape = CircleShape,
                color = AppPalette.Primary
            ) {
                Box(modifier = Modifier.padding(horizontal = 12.dp), contentAlignment = Alignment.CenterEnd) {
                    if (income > 0) {
                        Text(
                            text = String.format("%.0f %s", income, stringResource(R.string.currency_rub)),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}
