package by.dreb.tutorhelper.presentation.finance

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import by.dreb.tutorhelper.R
import by.dreb.tutorhelper.domain.model.LessonDetails
import by.dreb.tutorhelper.domain.model.Student
import by.dreb.tutorhelper.ui.components.MainContentCard
import by.dreb.tutorhelper.ui.components.TutorHelperHeader
import by.dreb.tutorhelper.ui.theme.StatusGreen
import by.dreb.tutorhelper.ui.theme.StatusOnGreen
import by.dreb.tutorhelper.ui.theme.StatusOnRed
import by.dreb.tutorhelper.ui.theme.StatusOnYellow
import by.dreb.tutorhelper.ui.theme.StatusRed
import by.dreb.tutorhelper.ui.theme.StatusYellow
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(viewModel: FinanceViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TutorHelperHeader(title = stringResource(R.string.finance_title))

        MainContentCard(modifier = Modifier.weight(1f)) {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                FinanceFilter.entries.forEachIndexed { index, filter ->
                    SegmentedButton(
                        selected = state.filter == filter,
                        onClick = { viewModel.updateFilter(filter) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = FinanceFilter.entries.size
                        ),
                        label = {
                            Text(
                                text = when (filter) {
                                    FinanceFilter.ACTIVE -> stringResource(R.string.finance_filter_unpaid)
                                    FinanceFilter.ARCHIVED -> stringResource(R.string.finance_filter_paid)
                                }
                            )
                        }
                    )
                }
            }

            if (state.listItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.finance_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
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
private fun StudentHeaderRow(
    student: Student,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = student.name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Icon(
            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
            .clickable { onTap() },
        colors = CardDefaults.cardColors(containerColor = cardTheme.backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = MaterialTheme.shapes.medium
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
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurface = MaterialTheme.colorScheme.onSurface
    val primary = MaterialTheme.colorScheme.primary

    return when {
        isArchive -> PaymentCardTheme(
            backgroundColor = StatusGreen.copy(alpha = 0.3f),
            textColor = StatusOnGreen,
            priceColor = StatusOnGreen,
            status1Color = StatusOnGreen,
            status2Color = StatusOnGreen
        )
        !isCompleted -> {
            if (isPaid) {
                PaymentCardTheme(
                    backgroundColor = surfaceVariant.copy(alpha = 0.3f),
                    textColor = onSurface,
                    priceColor = StatusOnGreen,
                    status1Color = onSurface.copy(alpha = 0.6f),
                    status2Color = StatusOnGreen
                )
            } else {
                PaymentCardTheme(
                    backgroundColor = surfaceVariant.copy(alpha = 0.3f),
                    textColor = onSurface,
                    priceColor = primary,
                    status1Color = onSurface.copy(alpha = 0.6f),
                    status2Color = onSurface.copy(alpha = 0.6f)
                )
            }
        }
        isCompleted && !isPaid -> PaymentCardTheme(
            backgroundColor = StatusYellow.copy(alpha = 0.3f),
            textColor = onSurface,
            priceColor = StatusOnRed,
            status1Color = StatusOnGreen,
            status2Color = StatusOnRed
        )
        else -> PaymentCardTheme( // Completed and Paid but not Homework sent (so not Archive yet)
            backgroundColor = surfaceVariant.copy(alpha = 0.5f),
            textColor = onSurface,
            priceColor = StatusOnGreen,
            status1Color = StatusOnGreen,
            status2Color = StatusOnGreen
        )
    }
}
