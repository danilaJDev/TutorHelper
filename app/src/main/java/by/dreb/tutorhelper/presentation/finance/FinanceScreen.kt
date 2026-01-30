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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import by.dreb.tutorhelper.ui.theme.InfoContainer
import by.dreb.tutorhelper.ui.theme.InfoText
import by.dreb.tutorhelper.ui.theme.NeutralContainer
import by.dreb.tutorhelper.ui.theme.NeutralText
import by.dreb.tutorhelper.ui.theme.SuccessContainer
import by.dreb.tutorhelper.ui.theme.SuccessText
import by.dreb.tutorhelper.ui.theme.WarningContainer
import by.dreb.tutorhelper.ui.theme.WarningText
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
        // 1. Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.finance_title),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Content Block
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
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
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
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

    // Case selection
    val backgroundColor: Color
    val priceColor: Color
    val status1Color: Color
    val status2Color: Color
    val textColor: Color

    when {
        isArchive -> {
            backgroundColor = SuccessContainer
            priceColor = SuccessText
            status1Color = SuccessText
            status2Color = SuccessText
            textColor = SuccessText
        }
        !isCompleted -> {
            backgroundColor = NeutralContainer
            if (isPaid) {
                priceColor = SuccessText
                status1Color = MaterialTheme.colorScheme.onSurfaceVariant
                status2Color = SuccessText
                textColor = MaterialTheme.colorScheme.onSurface
            } else {
                priceColor = NeutralText
                status1Color = MaterialTheme.colorScheme.onSurfaceVariant
                status2Color = MaterialTheme.colorScheme.onSurfaceVariant
                textColor = MaterialTheme.colorScheme.onSurface
            }
        }
        isCompleted && !isPaid -> {
            backgroundColor = WarningContainer
            priceColor = WarningText
            status1Color = SuccessText
            status2Color = WarningText
            textColor = MaterialTheme.colorScheme.onSurface
        }
        else -> {
            backgroundColor = InfoContainer
            priceColor = InfoText
            status1Color = InfoText
            status2Color = InfoText
            textColor = MaterialTheme.colorScheme.onSurface
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onTap() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
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
                    color = textColor
                )
                Text(
                    text = "${details.lesson.startTime.format(timeFormatter)} - ${endTime.format(timeFormatter)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isCompleted) stringResource(R.string.schedule_status_done) else stringResource(R.string.schedule_status_planned),
                        style = MaterialTheme.typography.labelMedium,
                        color = status1Color,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = when {
                            isPaid -> stringResource(R.string.finance_payment_status_paid)
                            isCompleted -> stringResource(R.string.finance_payment_status_unpaid)
                            else -> stringResource(R.string.finance_payment_status_future_unpaid)
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = status2Color,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Text(
                text = "${details.lesson.price} ${stringResource(R.string.currency_rub)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = priceColor
            )
        }
    }
}
