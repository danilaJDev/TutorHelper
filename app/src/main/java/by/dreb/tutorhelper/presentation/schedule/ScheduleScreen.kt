package by.dreb.tutorhelper.presentation.schedule

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import by.dreb.tutorhelper.R
import by.dreb.tutorhelper.domain.model.LessonDetails
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(viewModel: ScheduleViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    var selectedLessonForMenu by remember { mutableStateOf<LessonDetails?>(null) }

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
                text = stringResource(R.string.schedule_title),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            )

            IconButton(
                onClick = { /* TODO: Navigate to Add Lesson */ },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Combined Block (Tabs + Filters + Content)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                TabRow(
                    selectedTabIndex = state.mode.ordinal,
                    containerColor = Color.Transparent,
                    contentColor = Color.Black,
                    divider = {}
                ) {
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
                                    },
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        )
                    }
                }

                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    ScheduleFilter.entries.forEachIndexed { index, filter ->
                        SegmentedButton(
                            selected = state.filter == filter,
                            onClick = { viewModel.updateFilter(filter) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = ScheduleFilter.entries.size
                            ),
                            label = {
                                Text(
                                    text = when (filter) {
                                        ScheduleFilter.ACTIVE -> stringResource(R.string.schedule_filter_active)
                                        ScheduleFilter.HIDDEN -> stringResource(R.string.schedule_filter_hidden)
                                    }
                                )
                            }
                        )
                    }
                }

                when (state.mode) {
                    ScheduleMode.LIST -> ScheduleList(
                        lessons = state.lessons,
                        onMenuClick = { selectedLessonForMenu = it }
                    )
                    ScheduleMode.TABLE -> ScheduleTable(
                        lessons = state.lessons,
                        onMenuClick = { selectedLessonForMenu = it }
                    )
                    ScheduleMode.CALENDAR -> ScheduleCalendar(
                        lessons = state.lessons,
                        onMenuClick = { selectedLessonForMenu = it }
                    )
                }
            }
        }
    }

    selectedLessonForMenu?.let { lesson ->
        LessonActionsDialog(
            lesson = lesson,
            onDismiss = { selectedLessonForMenu = null },
            onToggleHidden = {
                viewModel.toggleHidden(lesson)
                selectedLessonForMenu = null
            },
            onToggleHomework = {
                viewModel.toggleHomework(lesson)
                selectedLessonForMenu = null
            },
            onToggleCompleted = {
                viewModel.toggleCompleted(lesson)
                selectedLessonForMenu = null
            }
        )
    }
}

@Composable
private fun ScheduleList(
    lessons: List<LessonDetails>,
    onMenuClick: (LessonDetails) -> Unit
) {
    if (lessons.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.schedule_empty),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(32.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    } else {
        val groupedLessons = lessons.groupBy { it.lesson.startTime.toLocalDate() }
        val sortedDates = groupedLessons.keys.sorted()
        val russianLocale = Locale("ru")

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            sortedDates.forEach { date ->
                item {
                    Text(
                        text = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy, EEEE", russianLocale)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    )
                }
                items(groupedLessons[date] ?: emptyList()) { lesson ->
                    LessonCard(
                        lesson = lesson,
                        onMenuClick = { onMenuClick(lesson) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ScheduleTable(
    lessons: List<LessonDetails>,
    onMenuClick: (LessonDetails) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(lessons) { lesson ->
            LessonCard(lesson, onMenuClick = { onMenuClick(lesson) }, compact = true)
        }
    }
}

@Composable
private fun ScheduleCalendar(
    lessons: List<LessonDetails>,
    onMenuClick: (LessonDetails) -> Unit
) {
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
        lessons.take(5).forEach { lesson ->
            LessonCard(lesson, onMenuClick = { onMenuClick(lesson) }, compact = true)
        }
    }
}

@Composable
private fun LessonCard(
    lesson: LessonDetails,
    onMenuClick: () -> Unit,
    compact: Boolean = false
) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val endTime = lesson.lesson.startTime.plusMinutes(lesson.lesson.durationMinutes.toLong())

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${lesson.lesson.startTime.format(timeFormatter)} - ${endTime.format(timeFormatter)}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = lesson.student.name,
                    style = MaterialTheme.typography.bodyLarge
                )
                if (!compact) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatusLabel(
                            text = if (lesson.lesson.isCompleted)
                                stringResource(R.string.schedule_status_done)
                            else
                                stringResource(R.string.schedule_status_planned)
                        )
                        StatusLabel(
                            text = if (lesson.lesson.isHomeworkSent)
                                stringResource(R.string.lesson_status_hw_sent)
                            else
                                stringResource(R.string.lesson_status_hw_not_sent)
                        )
                    }
                }
            }
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun StatusLabel(text: String) {
    val statusColors = when (text) {
        stringResource(R.string.schedule_status_planned) -> Pair(colorResource(R.color.status_grey), Color.DarkGray)
        stringResource(R.string.lesson_status_hw_not_sent) -> Pair(colorResource(R.color.status_yellow), Color(0xFF827717))
        stringResource(R.string.schedule_status_done), stringResource(R.string.lesson_status_hw_sent) -> Pair(colorResource(R.color.status_green), Color(0xFF1B5E20))
        else -> Pair(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
    }

    Surface(
        color = statusColors.first,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = statusColors.second
        )
    }
}

@Composable
private fun LessonActionsDialog(
    lesson: LessonDetails,
    onDismiss: () -> Unit,
    onToggleHidden: () -> Unit,
    onToggleHomework: () -> Unit,
    onToggleCompleted: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.lesson_actions_title),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TextButton(
                    onClick = onToggleCompleted,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.action_mark_completed))
                }
                TextButton(
                    onClick = onToggleHomework,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.action_mark_hw))
                }
                TextButton(
                    onClick = onToggleHidden,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (lesson.lesson.isHidden)
                            stringResource(R.string.action_show)
                        else
                            stringResource(R.string.action_hide)
                    )
                }
                TextButton(
                    onClick = { /* TODO: Edit */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.action_edit))
                }
                TextButton(
                    onClick = { /* TODO: Delete */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(text = stringResource(R.string.action_delete))
                }
            }
        },
        confirmButton = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(R.string.action_close))
                }
            }
        }
    )
}
