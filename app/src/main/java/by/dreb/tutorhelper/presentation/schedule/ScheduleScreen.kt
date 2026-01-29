package by.dreb.tutorhelper.presentation.schedule

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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import by.dreb.tutorhelper.R
import by.dreb.tutorhelper.domain.model.LessonDetails
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun ScheduleScreen(viewModel: ScheduleViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        ScheduleHeader(lessons = state.lessons)
        TabRow(selectedTabIndex = state.mode.ordinal) {
            ScheduleMode.entries.forEachIndexed { index, mode ->
                Tab(
                    selected = state.mode.ordinal == index,
                    onClick = { viewModel.updateMode(mode) },
                    text = {
                        Text(
                            text = when (mode) {
                                ScheduleMode.LIST -> stringResource(R.string.schedule_mode_list)
                                ScheduleMode.TABLE -> stringResource(R.string.schedule_mode_table)
                                ScheduleMode.CALENDAR -> stringResource(R.string.schedule_mode_calendar)
                            }
                        )
                    }
                )
            }
        }

        when (state.mode) {
            ScheduleMode.LIST -> ScheduleList(state.lessons)
            ScheduleMode.TABLE -> ScheduleTable(state.lessons)
            ScheduleMode.CALENDAR -> ScheduleCalendar(state.lessons)
        }
    }
}

@Composable
private fun ScheduleList(lessons: List<LessonDetails>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (lessons.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.schedule_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
        items(lessons) { lesson ->
            LessonCard(lesson)
        }
    }
}

@Composable
private fun ScheduleTable(lessons: List<LessonDetails>) {
    Surface(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(lessons) { lesson ->
                LessonCard(lesson, compact = true)
            }
        }
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
private fun ScheduleHeader(lessons: List<LessonDetails>) {
    val today = LocalDate.now()
    val todayLessons = lessons.count { it.lesson.startTime.toLocalDate() == today }
    val upcomingLessons = lessons.count { it.lesson.startTime.toLocalDate() >= today }

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = stringResource(R.string.schedule_title), style = MaterialTheme.typography.headlineSmall)
        Text(text = stringResource(R.string.schedule_subtitle))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(
                onClick = {},
                label = { Text(text = stringResource(R.string.schedule_today_count, todayLessons)) },
                leadingIcon = { androidx.compose.material3.Icon(Icons.Default.Event, contentDescription = null) },
                colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
            AssistChip(
                onClick = {},
                label = { Text(text = stringResource(R.string.schedule_upcoming_count, upcomingLessons)) },
                leadingIcon = { androidx.compose.material3.Icon(Icons.Default.AccessTime, contentDescription = null) }
            )
        }
    }
}

@Composable
private fun LessonCard(lesson: LessonDetails, compact: Boolean = false) {
    val timeFormatter = DateTimeFormatter.ofPattern("dd MMM, HH:mm")
    val titleStyle = if (compact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium
    val bodyStyle = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.material3.Icon(Icons.Default.Person, contentDescription = null)
                Text(
                    text = lesson.student.name,
                    style = titleStyle,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Text(text = lesson.lesson.subject, style = bodyStyle)
            Text(text = lesson.lesson.startTime.format(timeFormatter), style = bodyStyle)
            Text(
                text = stringResource(R.string.lesson_duration_price, lesson.lesson.durationMinutes, lesson.lesson.price),
                style = bodyStyle
            )
        }
    }
}
