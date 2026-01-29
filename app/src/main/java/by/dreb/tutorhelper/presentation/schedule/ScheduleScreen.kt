package by.dreb.tutorhelper.presentation.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.TableRows
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import by.dreb.tutorhelper.R
import by.dreb.tutorhelper.domain.model.LessonDetails
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import java.util.Locale

@Composable
fun ScheduleScreen(viewModel: ScheduleViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        ScheduleHeader(
            mode = state.mode,
            showHidden = state.showHidden,
            onModeSelected = viewModel::updateMode,
            onHiddenSelected = viewModel::updateHidden,
            onAddClick = {}
        }

        when (state.mode) {
            ScheduleMode.LIST -> ScheduleList(state.lessons, state.showHidden)
            ScheduleMode.TABLE -> ScheduleTable(state.lessons, state.showHidden)
            ScheduleMode.CALENDAR -> ScheduleCalendar(state.lessons)
        }
    }
}

@Composable
private fun ScheduleList(lessons: List<LessonDetails>, showHidden: Boolean) {
    val now = LocalDateTime.now()
    val filtered = lessons.filter { lesson ->
        val isPast = lesson.lesson.startTime.isBefore(now)
        if (showHidden) isPast else !isPast
    }

    if (filtered.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.schedule_empty),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        return
    }

    val grouped = filtered
        .groupBy { it.lesson.startTime.toLocalDate() }
        .toSortedMap()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        grouped.forEach { (date, dayLessons) ->
            item {
                Text(
                    text = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy, EEEE", Locale("ru"))),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
            items(dayLessons) { lesson ->
                LessonCard(lesson)
            }
        }
    }
}

@Composable
private fun ScheduleTable(lessons: List<LessonDetails>, showHidden: Boolean) {
    Surface(modifier = Modifier.fillMaxSize()) {
        ScheduleList(lessons = lessons, showHidden = showHidden)
    }
}

@Composable
private fun ScheduleCalendar(lessons: List<LessonDetails>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.schedule_calendar_hint),
            style = MaterialTheme.typography.titleMedium
        )
        lessons.take(3).forEach { lesson ->
            LessonCard(lesson, compact = true)
        }
    }
}

@Composable
private fun ScheduleHeader(
    mode: ScheduleMode,
    showHidden: Boolean,
    onModeSelected: (ScheduleMode) -> Unit,
    onHiddenSelected: (Boolean) -> Unit,
    onAddClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.schedule_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f)
                ) {
                    IconButton(onClick = onAddClick) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
            TabRow(
                selectedTabIndex = mode.ordinal,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                ScheduleMode.entries.forEachIndexed { index, tabMode ->
                    Tab(
                        selected = mode.ordinal == index,
                        onClick = { onModeSelected(tabMode) },
                        icon = {
                            Icon(
                                imageVector = when (tabMode) {
                                    ScheduleMode.LIST -> Icons.Default.ListAlt
                                    ScheduleMode.TABLE -> Icons.Default.TableRows
                                    ScheduleMode.CALENDAR -> Icons.Default.CalendarMonth
                                },
                                contentDescription = null
                            )
                        },
                        text = {
                            Text(
                                text = when (tabMode) {
                                    ScheduleMode.LIST -> stringResource(R.string.schedule_mode_list)
                                    ScheduleMode.TABLE -> stringResource(R.string.schedule_mode_table)
                                    ScheduleMode.CALENDAR -> stringResource(R.string.schedule_mode_calendar)
                                }
                            )
                        }
                    )
                }
            }
            Surface(
                color = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .padding(6.dp)
                        .fillMaxWidth()
                ) {
                    SegmentedButton(
                        selected = !showHidden,
                        onClick = { onHiddenSelected(false) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Text(text = stringResource(R.string.schedule_active))
                    }
                    SegmentedButton(
                        selected = showHidden,
                        onClick = { onHiddenSelected(true) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Text(text = stringResource(R.string.schedule_hidden))
                    }
                }
            }
        }
    }
}

@Composable
private fun LessonCard(lesson: LessonDetails, compact: Boolean = false) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val titleStyle = if (compact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium
    val bodyStyle = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium
    val endTime = lesson.lesson.startTime.plusMinutes(lesson.lesson.durationMinutes.toLong())
    val isPast = lesson.lesson.startTime.isBefore(LocalDateTime.now())
    val statusText = if (isPast) {
        stringResource(R.string.schedule_status_done)
    } else {
        stringResource(R.string.schedule_status_planned)
    }
    val homeworkText = if (lesson.lesson.id % 2L == 0L) {
        stringResource(R.string.schedule_status_homework_sent)
    } else {
        stringResource(R.string.schedule_status_homework_missing)
    }
    var showMenu by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${lesson.lesson.startTime.format(timeFormatter)} - ${endTime.format(timeFormatter)}",
                    style = titleStyle
                )
                Box(modifier = Modifier.weight(1f))
                IconButton(onClick = { showMenu = true }) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = null)
                }
            }
            Text(text = lesson.student.name, style = bodyStyle)
            Text(text = lesson.lesson.subject, style = bodyStyle)
            Text(
                text = stringResource(R.string.lesson_duration_price, lesson.lesson.durationMinutes, lesson.lesson.price),
                style = bodyStyle
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = {},
                    label = { Text(text = statusText) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
                AssistChip(
                    onClick = {},
                    label = { Text(text = homeworkText) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
            }
        }
    }

    if (showMenu) {
        AlertDialog(
            onDismissRequest = { showMenu = false },
            title = { Text(text = stringResource(R.string.schedule_menu_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = stringResource(R.string.schedule_menu_edit))
                    Text(text = stringResource(R.string.schedule_menu_mark_done))
                    Text(text = stringResource(R.string.schedule_menu_hide))
                }
            },
            confirmButton = {
                TextButton(onClick = { showMenu = false }) {
                    Text(text = stringResource(R.string.schedule_menu_close))
                }
            }
        )
    }
}
