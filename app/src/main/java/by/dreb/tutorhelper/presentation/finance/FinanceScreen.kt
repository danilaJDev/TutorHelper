package by.dreb.tutorhelper.presentation.finance

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import by.dreb.tutorhelper.R
import by.dreb.tutorhelper.domain.model.LessonDetails
import by.dreb.tutorhelper.domain.model.Student
import by.dreb.tutorhelper.presentation.common.AppPalette
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(viewModel: FinanceViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.finance_title),
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
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FinanceFilterChip(
                    selected = state.filter == FinanceFilter.ACTIVE,
                    onClick = { viewModel.updateFilter(FinanceFilter.ACTIVE) },
                    label = stringResource(R.string.finance_filter_unpaid)
                )
                Spacer(modifier = Modifier.width(12.dp))
                FinanceFilterChip(
                    selected = state.filter == FinanceFilter.ARCHIVED,
                    onClick = { viewModel.updateFilter(FinanceFilter.ARCHIVED) },
                    label = stringResource(R.string.finance_filter_paid)
                )
            }

            if (state.listItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = AppPalette.TextSecondary.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.finance_empty),
                            style = MaterialTheme.typography.titleMedium,
                            color = AppPalette.TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(state.listItems, key = { item ->
                        when (item) {
                            is FinanceListItem.StudentHeader -> "student_${item.student.id}"
                            is FinanceListItem.LessonItem -> "lesson_${item.details.lesson.id}"
                        }
                    }) { item ->
                        when (item) {
                            is FinanceListItem.StudentHeader -> StudentHeaderRow(
                                student = item.student,
                                isExpanded = item.isExpanded,
                                onToggle = { viewModel.toggleStudentExpanded(item.student.id) }
                            )
                            is FinanceListItem.LessonItem -> LessonPaymentCard(
                                details = item.details,
                                onTap = { viewModel.togglePayment(item.details) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FinanceFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = AppPalette.Primary.copy(alpha = 0.15f),
            selectedLabelColor = AppPalette.Primary,
            containerColor = AppPalette.Surface,
            labelColor = AppPalette.TextSecondary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = if (selected) AppPalette.Primary else Color.Transparent,
            selectedBorderColor = AppPalette.Primary
        )
    )
}

@Composable
private fun StudentHeaderRow(
    student: Student,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .border(1.dp, Color(0xFFE2E8F0), AppPalette.CardShape)
            .shadow(2.dp, AppPalette.CardShape)
            .clip(AppPalette.CardShape)
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(containerColor = AppPalette.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = AppPalette.CardElevation),
        shape = AppPalette.CardShape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = student.name,
                style = MaterialTheme.typography.titleMedium,
                color = AppPalette.TextPrimary
            )
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = AppPalette.TextSecondary
            )
        }
    }
}

@Composable
private fun LessonPaymentCard(
    details: LessonDetails,
    onTap: () -> Unit
) {
    val russianLocale = Locale("ru")
    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy, EEEE", russianLocale)
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val endTime = details.lesson.startTime.plusMinutes(details.lesson.durationMinutes.toLong())

    val isCompleted = details.lesson.isCompleted
    val isPaid = details.payment != null
    val isHomeworkSent = details.lesson.isHomeworkSent
    val isArchive = isCompleted && isHomeworkSent && isPaid

    val cardTheme = getPaymentCardTheme(isCompleted, isPaid, isArchive)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .border(1.dp, Color(0xFFE2E8F0), AppPalette.CardShape)
            .shadow(2.dp, AppPalette.CardShape)
            .clip(AppPalette.CardShape)
            .clickable { onTap() },
        colors = CardDefaults.cardColors(containerColor = cardTheme.backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = AppPalette.CardElevation),
        shape = AppPalette.CardShape
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = details.lesson.startTime.format(dateFormatter),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = cardTheme.textColor
                )
                Text(
                    text = "${details.lesson.startTime.format(timeFormatter)} - ${endTime.format(timeFormatter)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = cardTheme.textColor.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isCompleted) stringResource(R.string.schedule_status_done) else stringResource(R.string.schedule_status_planned),
                        style = MaterialTheme.typography.labelMedium,
                        color = cardTheme.status1Color,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = when {
                            isPaid -> stringResource(R.string.finance_payment_status_paid)
                            isCompleted -> stringResource(R.string.finance_payment_status_unpaid)
                            else -> stringResource(R.string.finance_payment_status_future_unpaid)
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = cardTheme.status2Color,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Text(
                text = "${details.lesson.price} ${stringResource(R.string.currency_rub)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = cardTheme.priceColor
            )
        }
    }
}

private data class PaymentCardTheme(
    val backgroundColor: Color,
    val textColor: Color,
    val priceColor: Color,
    val status1Color: Color,
    val status2Color: Color
)

@Composable
private fun getPaymentCardTheme(
    isCompleted: Boolean,
    isPaid: Boolean,
    isArchive: Boolean
): PaymentCardTheme {
    val surfaceVariant = AppPalette.Surface
    val onSurface = AppPalette.TextPrimary
    val primary = AppPalette.Primary

    return when {
        isArchive -> PaymentCardTheme(
            backgroundColor = AppPalette.Success.copy(alpha = 0.15f),
            textColor = AppPalette.TextPrimary,
            priceColor = AppPalette.Success,
            status1Color = AppPalette.Success,
            status2Color = AppPalette.Success
        )
        !isCompleted -> {
            if (isPaid) {
                PaymentCardTheme(
                    backgroundColor = surfaceVariant,
                    textColor = onSurface,
                    priceColor = AppPalette.Success,
                    status1Color = AppPalette.TextSecondary,
                    status2Color = AppPalette.Success
                )
            } else {
                PaymentCardTheme(
                    backgroundColor = surfaceVariant,
                    textColor = onSurface,
                    priceColor = primary,
                    status1Color = AppPalette.TextSecondary,
                    status2Color = AppPalette.TextSecondary
                )
            }
        }
        isCompleted && !isPaid -> PaymentCardTheme(
            backgroundColor = AppPalette.Action.copy(alpha = 0.2f),
            textColor = onSurface,
            priceColor = AppPalette.Error,
            status1Color = AppPalette.Success,
            status2Color = AppPalette.Error
        )
        else -> PaymentCardTheme( // Completed and Paid but not Homework sent (so not Archive yet)
            backgroundColor = surfaceVariant,
            textColor = onSurface,
            priceColor = AppPalette.Success,
            status1Color = AppPalette.Success,
            status2Color = AppPalette.Success
        )
    }
}
