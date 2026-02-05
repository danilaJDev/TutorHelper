package by.dreb.tutorhelper.presentation.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import by.dreb.tutorhelper.R
import by.dreb.tutorhelper.domain.model.LessonDetails
import by.dreb.tutorhelper.domain.model.Student
import by.dreb.tutorhelper.ui.components.TutorHelperEmptyState
import by.dreb.tutorhelper.ui.components.TutorHelperFilterChip
import by.dreb.tutorhelper.ui.components.TutorHelperTopAppBar
import by.dreb.tutorhelper.ui.theme.AppPalette
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(viewModel: FinanceViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TutorHelperTopAppBar(title = stringResource(R.string.finance_title))
        },
        containerColor = AppPalette.Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Фильтры
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FinanceFilter.entries.forEachIndexed { index, filter ->
                    TutorHelperFilterChip(
                        selected = state.filter == filter,
                        onClick = { viewModel.updateFilter(filter) },
                        label = when (filter) {
                            FinanceFilter.ACTIVE -> stringResource(R.string.finance_filter_unpaid)
                            FinanceFilter.ARCHIVED -> stringResource(R.string.finance_filter_paid)
                        },
                        icon = when (filter) {
                            FinanceFilter.ACTIVE -> Icons.Default.CheckCircle
                            FinanceFilter.ARCHIVED -> Icons.Default.Archive
                        }
                    )
                    if (index < FinanceFilter.entries.size - 1) {
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                }
            }

            if (state.listItems.isEmpty()) {
                TutorHelperEmptyState(
                    message = stringResource(R.string.finance_empty),
                    icon = Icons.Default.Payments,
                    modifier = Modifier.weight(1f)
                )
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

                            is FinanceListItem.LessonItem -> ModernFinanceCard(
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
            fontWeight = FontWeight.Bold,
            color = AppPalette.TextPrimary
        )
        Icon(
            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = AppPalette.TextSecondary
        )
    }
}

@Composable
private fun ModernFinanceCard(
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

    val statusColor = when {
        isArchive -> AppPalette.Success
        isCompleted && !isPaid -> AppPalette.Action
        isPaid -> AppPalette.Success
        else -> AppPalette.TextSecondary.copy(alpha = 0.3f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .border(1.dp, AppPalette.Outline, AppPalette.CardShape)
            .shadow(1.dp, AppPalette.CardShape)
            .clip(AppPalette.CardShape)
            .clickable { onTap() },
        colors = CardDefaults.cardColors(containerColor = AppPalette.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = AppPalette.CardElevation),
        shape = AppPalette.CardShape
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(statusColor)
            )

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
                        color = AppPalette.TextPrimary
                    )
                    Text(
                        text = "${details.lesson.startTime.format(timeFormatter)} - ${
                            endTime.format(
                                timeFormatter
                            )
                        }",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppPalette.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (isCompleted) stringResource(R.string.schedule_status_done) else stringResource(
                                R.string.schedule_status_planned
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isCompleted) AppPalette.Success else AppPalette.TextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = when {
                                isPaid -> stringResource(R.string.finance_payment_status_paid)
                                isCompleted -> stringResource(R.string.finance_payment_status_unpaid)
                                else -> stringResource(R.string.finance_payment_status_future_unpaid)
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isPaid) AppPalette.Success else if (isCompleted) AppPalette.Error else AppPalette.TextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Text(
                    text = "${details.lesson.price} ${stringResource(R.string.currency_rub)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isPaid) AppPalette.Success else AppPalette.TextPrimary
                )
            }
        }
    }
}
