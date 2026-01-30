package by.dreb.tutorhelper.presentation.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import by.dreb.tutorhelper.R
import by.dreb.tutorhelper.domain.model.LessonDetails
import by.dreb.tutorhelper.ui.components.MainContentCard
import by.dreb.tutorhelper.ui.components.TutorHelperHeader
import by.dreb.tutorhelper.ui.theme.StatusGreen
import by.dreb.tutorhelper.ui.theme.StatusOnGreen
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    onLessonClick: (Long) -> Unit,
    onAddLessonClick: () -> Unit,
    viewModel: ScheduleViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var lessonToDelete by remember { mutableStateOf<LessonDetails?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TutorHelperHeader(
            title = stringResource(R.string.schedule_title),
            actionIcon = Icons.Default.Add,
            onActionClick = onAddLessonClick
        )

        MainContentCard(modifier = Modifier.weight(1f)) {
            Column(modifier = Modifier.padding(16.dp)) {
                val modeTabs = ScheduleMode.entries
                TabRow(
                    selectedTabIndex = modeTabs.indexOf(state.mode),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    modeTabs.forEach { mode ->
                        Tab(
                            selected = state.mode == mode,
                            onClick = { viewModel.updateMode(mode) },
                            text = {
                                Text(
                                    text = when (mode) {
                                        ScheduleMode.LIST -> stringResource(R.string.schedule_mode_list)
                                        ScheduleMode.CALENDAR -> stringResource(R.string.schedule_mode_calendar)
                                    }
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
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
            }

            when (state.mode) {
                ScheduleMode.LIST -> ScheduleList(
                    lessons = state.lessons,
                    onLessonClick = onLessonClick,
                    onToggleHomework = viewModel::toggleHomework,
                    onToggleHidden = viewModel::toggleHidden,
                    onDeleteClick = { lessonToDelete = it }
                )

                ScheduleMode.CALENDAR -> ScheduleCalendar(
                    selectedDate = state.selectedDate,
                    lessons = state.lessons,
                    onDateSelected = { viewModel.updateSelectedDate(it) },
                    onLessonClick = onLessonClick,
                    onToggleHomework = viewModel::toggleHomework,
                    onToggleHidden = viewModel::toggleHidden,
                    onDeleteClick = { lessonToDelete = it }
                )
            }
        }
    }

    lessonToDelete?.let { lesson ->
        AlertDialog(
            onDismissRequest = { lessonToDelete = null },
            title = {
                Text(
                    text = stringResource(R.string.action_delete_confirm_title),
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.action_delete_confirm_message),
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteLesson(lesson)
                        lessonToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(
                        text = stringResource(R.string.action_delete),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { lessonToDelete = null }) {
                    Text(text = stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun ScheduleList(
    lessons: List<LessonDetails>,
    onLessonClick: (Long) -> Unit,
    onToggleHomework: (LessonDetails) -> Unit,
    onToggleHidden: (LessonDetails) -> Unit,
    onDeleteClick: (LessonDetails) -> Unit
) {
    if (lessons.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.schedule_empty),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(32.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        val groupedLessons = lessons.groupBy { it.lesson.startTime.toLocalDate() }
        val sortedDates = groupedLessons.keys.sorted()
        val russianLocale = Locale("ru")

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            sortedDates.forEach { date ->
                item {
                    Text(
                        text = date.format(
                            DateTimeFormatter.ofPattern(
                                "dd.MM.yyyy, EEEE",
                                russianLocale
                            )
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 8.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                items(groupedLessons[date] ?: emptyList()) { lesson ->
                    LessonCard(
                        lesson = lesson,
                        onLessonClick = { onLessonClick(lesson.lesson.id) },
                        onToggleHomework = { onToggleHomework(lesson) },
                        onToggleHidden = { onToggleHidden(lesson) },
                        onDeleteClick = { onDeleteClick(lesson) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ScheduleCalendar(
    selectedDate: LocalDate,
    lessons: List<LessonDetails>,
    onDateSelected: (LocalDate) -> Unit,
    onLessonClick: (Long) -> Unit,
    onToggleHomework: (LessonDetails) -> Unit,
    onToggleHidden: (LessonDetails) -> Unit,
    onDeleteClick: (LessonDetails) -> Unit
) {
    var currentMonth by remember { mutableStateOf(selectedDate.withDayOfMonth(1)) }
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfWeek = currentMonth.dayOfWeek.value // 1 (Mon) to 7 (Sun)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Month Navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = null
                )
            }
            Text(
                text = currentMonth.format(DateTimeFormatter.ofPattern("LLLL yyyy", Locale("ru")))
                    .replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Days of week headers
        Row(modifier = Modifier.fillMaxWidth()) {
            val weekDays = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
            weekDays.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Calendar Grid
        val totalCells = ((daysInMonth + firstDayOfWeek - 2) / 7 + 1) * 7
        Column {
            for (row in 0 until totalCells / 7) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0 until 7) {
                        val dayIndex = row * 7 + col
                        val dayOfMonth = dayIndex - firstDayOfWeek + 2
                        if (dayOfMonth in 1..daysInMonth) {
                            val date = currentMonth.withDayOfMonth(dayOfMonth)
                            val isSelected = date == selectedDate
                            val dayLessons =
                                lessons.filter { it.lesson.startTime.toLocalDate() == date }

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onDateSelected(date) },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = dayOfMonth.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                                if (dayLessons.isNotEmpty()) {
                                    val hasIncomplete = dayLessons.any { !it.lesson.isCompleted }
                                    val dotColor =
                                        if (hasIncomplete) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .background(dotColor, CircleShape)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.size(4.dp))
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lessons for selected date
        val selectedDayLessons =
            lessons.filter { it.lesson.startTime.toLocalDate() == selectedDate }
        if (selectedDayLessons.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Нет занятий на этот день",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(selectedDayLessons) { lesson ->
                    LessonCard(
                        lesson = lesson,
                        onLessonClick = { onLessonClick(lesson.lesson.id) },
                        onToggleHomework = { onToggleHomework(lesson) },
                        onToggleHidden = { onToggleHidden(lesson) },
                        onDeleteClick = { onDeleteClick(lesson) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LessonCard(
    lesson: LessonDetails,
    onLessonClick: () -> Unit,
    onToggleHomework: () -> Unit,
    onToggleHidden: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val endTime = lesson.lesson.startTime.plusMinutes(lesson.lesson.durationMinutes.toLong())
    val showHide = lesson.lesson.isHomeworkSent && lesson.lesson.isCompleted

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (lesson.lesson.isCompleted && lesson.lesson.isHomeworkSent) {
                StatusGreen.copy(alpha = 0.16f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(
                    alpha = 0.5f
                )
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onLessonClick() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(32.dp),
                contentAlignment = Alignment.Center
            ) {
                LessonStatusIcon(isCompleted = lesson.lesson.isCompleted)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lesson.student.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "${lesson.lesson.startTime.format(timeFormatter)} - ${
                        endTime.format(
                            timeFormatter
                        )
                    }",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HomeworkStatusButton(
                    isSent = lesson.lesson.isHomeworkSent,
                    onClick = onToggleHomework
                )
                if (showHide) {
                    HideLessonButton(onClick = onToggleHidden)
                } else {
                    DeleteLessonButton(onClick = onDeleteClick)
                }
            }
        }
    }
}

@Composable
private fun HomeworkStatusButton(
    isSent: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isSent) StatusGreen else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isSent) StatusOnGreen else MaterialTheme.colorScheme.onSurfaceVariant
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(36.dp)
            .background(containerColor, CircleShape)
            .border(1.dp, Color.Black, CircleShape)
    ) {
        Icon(
            imageVector = Icons.Default.Home,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun DeleteLessonButton(onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(36.dp)
            .background(Color.Transparent, CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.error, CircleShape)
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun HideLessonButton(onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(36.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
            .border(1.dp, Color.Black, CircleShape)
    ) {
        Icon(
            imageVector = Icons.Default.VisibilityOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun LessonStatusIcon(isCompleted: Boolean) {
    val containerColor =
        if (isCompleted) StatusGreen else MaterialTheme.colorScheme.surfaceVariant
    val contentColor =
        if (isCompleted) StatusOnGreen else MaterialTheme.colorScheme.onSurfaceVariant
    val icon = if (isCompleted) Icons.Default.Check else Icons.Default.Schedule

    Box(
        modifier = Modifier
            .size(28.dp)
            .background(containerColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(16.dp)
        )
    }
}
