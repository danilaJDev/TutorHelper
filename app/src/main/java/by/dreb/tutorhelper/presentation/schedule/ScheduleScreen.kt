package by.dreb.tutorhelper.presentation.schedule

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import by.dreb.tutorhelper.R
import by.dreb.tutorhelper.domain.model.LessonDetails
import by.dreb.tutorhelper.ui.components.TutorHelperEmptyState
import by.dreb.tutorhelper.ui.components.TutorHelperFilterChip
import by.dreb.tutorhelper.ui.components.TutorHelperTopAppBar
import by.dreb.tutorhelper.ui.theme.AppPalette
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    onLessonClick: (Long) -> Unit,
    onAddLessonClick: (LocalDate) -> Unit,
    viewModel: ScheduleViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var lessonToDelete by remember { mutableStateOf<LessonDetails?>(null) }
    var deleteFutureLessons by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TutorHelperTopAppBar(
                title = stringResource(R.string.schedule_title),
                actions = {
                    ScheduleModeSwitch(
                        currentMode = state.mode,
                        onModeChange = { viewModel.updateMode(it) }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddLessonClick(state.selectedDate) },
                containerColor = AppPalette.Primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Lesson")
            }
        },
        containerColor = AppPalette.Background
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.mode == ScheduleMode.LIST) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TutorHelperFilterChip(
                        selected = state.filter == ScheduleFilter.ACTIVE,
                        onClick = { viewModel.updateFilter(ScheduleFilter.ACTIVE) },
                        label = stringResource(R.string.schedule_filter_active),
                        icon = Icons.Default.CheckCircle
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    TutorHelperFilterChip(
                        selected = state.filter == ScheduleFilter.HIDDEN,
                        onClick = { viewModel.updateFilter(ScheduleFilter.HIDDEN) },
                        label = stringResource(R.string.schedule_filter_hidden),
                        icon = Icons.Default.VisibilityOff
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                AnimatedContent(
                    targetState = state.mode,
                    label = "ModeAnimation"
                ) { mode ->
                    when (mode) {
                        ScheduleMode.LIST -> ScheduleList(
                            lessons = state.lessons,
                            onLessonClick = onLessonClick,
                            onToggleHomework = viewModel::toggleHomework,
                            onToggleHidden = viewModel::toggleHidden,
                            onDeleteClick = {
                                lessonToDelete = it
                                deleteFutureLessons = false
                            }
                        )

                        ScheduleMode.CALENDAR -> ScheduleCalendar(
                            selectedDate = state.selectedDate,
                            lessons = state.lessons,
                            onDateSelected = { viewModel.updateSelectedDate(it) },
                            onLessonClick = onLessonClick,
                            onToggleHomework = viewModel::toggleHomework,
                            onToggleHidden = viewModel::toggleHidden,
                            onDeleteClick = {
                                lessonToDelete = it
                                deleteFutureLessons = false
                            }
                        )
                    }
                }
            }
        }
    }

    if (lessonToDelete != null) {
        DeleteConfirmationDialog(
            onDismiss = { lessonToDelete = null },
            onConfirm = {
                lessonToDelete?.let {
                    if (deleteFutureLessons) {
                        viewModel.deleteLessonWithFutureDuplicates(it)
                    } else {
                        viewModel.deleteLesson(it)
                    }
                }
                lessonToDelete = null
            },
            deleteFutureLessons = deleteFutureLessons,
            onDeleteFutureLessonsChange = { deleteFutureLessons = it }
        )
    }
}

@Composable
private fun ScheduleModeSwitch(
    currentMode: ScheduleMode,
    onModeChange: (ScheduleMode) -> Unit
) {
    Row(
        modifier = Modifier
            .width(100.dp)
            .height(40.dp)
            .background(
                color = AppPalette.Outline,
                shape = CircleShape
            )
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val modes = listOf(
            ScheduleMode.LIST to Icons.Default.FormatListBulleted,
            ScheduleMode.CALENDAR to Icons.Default.CalendarMonth
        )

        modes.forEach { (mode, icon) ->
            val isSelected = currentMode == mode
            val background by animateColorAsState(
                if (isSelected) AppPalette.Surface else Color.Transparent,
                label = "switchBg"
            )
            val iconColor by animateColorAsState(
                if (isSelected) AppPalette.Primary else AppPalette.TextSecondary,
                label = "switchIcon"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(background)
                    .clickable { onModeChange(mode) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ModeIconButton(
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) AppPalette.Primary else Color.Transparent,
        label = "BgColor"
    )
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else AppPalette.TextSecondary,
        label = "IconColor"
    )

    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(36.dp)
            .background(backgroundColor, CircleShape)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
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
        TutorHelperEmptyState(
            message = stringResource(R.string.schedule_empty),
            icon = Icons.Default.CalendarMonth,
            modifier = Modifier.fillMaxSize()
        )
    } else {
        val groupedLessons = lessons.groupBy { it.lesson.startTime.toLocalDate() }
        val sortedDates = groupedLessons.keys.sorted()
        val russianLocale = Locale("ru")

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            sortedDates.forEach { date ->
                item {
                    DateHeader(date, russianLocale)
                }
                items(groupedLessons[date] ?: emptyList()) { lesson ->
                    ModernLessonCard(
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
private fun DateHeader(date: LocalDate, locale: Locale) {
    val isToday = date == LocalDate.now()
    val dateText = date.format(DateTimeFormatter.ofPattern("d MMMM", locale))
    val weekDayText = date.format(DateTimeFormatter.ofPattern("EEEE", locale))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isToday) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(AppPalette.Primary, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Text(
            text = dateText,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isToday) AppPalette.Primary else AppPalette.TextPrimary
        )
        Text(
            text = " • ${weekDayText.replaceFirstChar { it.uppercase() }}",
            style = MaterialTheme.typography.titleMedium,
            color = AppPalette.TextSecondary
        )
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
    val firstDayOfWeek = currentMonth.dayOfWeek.value
    val russianLocale = Locale("ru")

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            color = AppPalette.Surface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            null,
                            tint = AppPalette.TextSecondary
                        )
                    }
                    Text(
                        text = currentMonth.format(
                            DateTimeFormatter.ofPattern(
                                "LLLL yyyy",
                                russianLocale
                            )
                        )
                            .replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppPalette.TextPrimary
                    )
                    IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            null,
                            tint = AppPalette.TextSecondary
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    val weekDays = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
                    weekDays.forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AppPalette.TextSecondary
                        )
                    }
                }

                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    val totalCells = ((daysInMonth + firstDayOfWeek - 2) / 7 + 1) * 7
                    for (row in 0 until totalCells / 7) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            for (col in 0 until 7) {
                                val dayIndex = row * 7 + col
                                val dayOfMonth = dayIndex - firstDayOfWeek + 2
                                if (dayOfMonth in 1..daysInMonth) {
                                    val date = currentMonth.withDayOfMonth(dayOfMonth)
                                    val isSelected = date == selectedDate
                                    val isToday = date == LocalDate.now()
                                    val dayLessons =
                                        lessons.filter { it.lesson.startTime.toLocalDate() == date }

                                    DayCell(
                                        day = dayOfMonth,
                                        isSelected = isSelected,
                                        isToday = isToday,
                                        hasEvents = dayLessons.isNotEmpty(),
                                        hasIncomplete = dayLessons.any { !it.lesson.isCompleted },
                                        onClick = { onDateSelected(date) },
                                        modifier = Modifier.weight(1f)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = selectedDate.format(DateTimeFormatter.ofPattern("d MMMM, EEEE", russianLocale)),
            style = MaterialTheme.typography.titleMedium,
            color = AppPalette.TextPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        val selectedDayLessons =
            lessons.filter { it.lesson.startTime.toLocalDate() == selectedDate }

        LazyColumn(
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (selectedDayLessons.isEmpty()) {
                item {
                    TutorHelperEmptyState(
                        message = stringResource(R.string.schedule_empty),
                        icon = Icons.Default.CalendarMonth,
                        modifier = Modifier.fillParentMaxSize()
                    )
                }
            } else {
                items(selectedDayLessons) { lesson ->
                    ModernLessonCard(
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
private fun DayCell(
    day: Int,
    isSelected: Boolean,
    isToday: Boolean,
    hasEvents: Boolean,
    hasIncomplete: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isSelected -> AppPalette.Primary
        else -> Color.Transparent
    }

    val contentColor = when {
        isSelected -> Color.White
        isToday -> AppPalette.Primary
        else -> AppPalette.TextPrimary
    }

    val borderModifier = if (isToday && !isSelected) {
        Modifier.border(1.dp, AppPalette.Primary, CircleShape)
    } else Modifier

    Column(
        modifier = modifier
            .height(44.dp)
            .clip(CircleShape)
            .then(borderModifier)
            .background(backgroundColor, CircleShape)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
            color = contentColor
        )

        if (hasEvents) {
            Spacer(modifier = Modifier.height(4.dp))
            val dotColor = if (isSelected) Color.White.copy(alpha = 0.8f)
            else if (hasIncomplete) AppPalette.Primary
            else AppPalette.Success
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(dotColor, CircleShape)
            )
        } else {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ModernLessonCard(
    lesson: LessonDetails,
    onLessonClick: () -> Unit,
    onToggleHomework: () -> Unit,
    onToggleHidden: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val startTime = lesson.lesson.startTime.format(timeFormatter)
    val endTime = lesson.lesson.startTime
        .plusMinutes(lesson.lesson.durationMinutes.toLong())
        .format(timeFormatter)

    val isCompleted = lesson.lesson.isCompleted
    val isHomeworkSent = lesson.lesson.isHomeworkSent
    val isHidden = isCompleted && isHomeworkSent

    val statusColor = when {
        isHomeworkSent -> AppPalette.Success
        isCompleted -> AppPalette.Success
        else -> AppPalette.Primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .border(1.dp, AppPalette.Outline, AppPalette.CardShape)
            .shadow(2.dp, AppPalette.CardShape)
            .clip(AppPalette.CardShape)
            .clickable(onClick = onLessonClick),
        elevation = CardDefaults.cardElevation(defaultElevation = AppPalette.CardElevation),
        colors = CardDefaults.cardColors(containerColor = AppPalette.Surface),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.width(60.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = startTime,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppPalette.TextPrimary
                    )
                    Text(
                        text = endTime,
                        style = MaterialTheme.typography.labelMedium,
                        color = AppPalette.TextSecondary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = lesson.student.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = AppPalette.TextPrimary,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isCompleted) {
                            Icon(
                                Icons.Default.CheckCircle,
                                null,
                                modifier = Modifier.size(14.dp),
                                tint = AppPalette.Success
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.schedule_status_done),
                                style = MaterialTheme.typography.labelSmall,
                                color = AppPalette.Success,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.schedule_status_planned),
                                style = MaterialTheme.typography.labelSmall,
                                color = AppPalette.Primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Row {
                    val hwTint =
                        if (isHomeworkSent) AppPalette.Success else AppPalette.TextSecondary.copy(
                            alpha = 0.5f
                        )
                    val hwBg =
                        if (isHomeworkSent) AppPalette.Success.copy(alpha = 0.1f) else Color.Transparent

                    IconButton(
                        onClick = onToggleHomework,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = hwBg,
                            contentColor = hwTint
                        ),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (isHomeworkSent) Icons.Filled.Home else Icons.Outlined.Home,
                            contentDescription = "Homework",
                            modifier = Modifier.size(25.dp)
                        )
                    }

                    if (isHidden) {
                        IconButton(
                            onClick = onToggleHidden,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VisibilityOff,
                                contentDescription = "Hide",
                                tint = AppPalette.TextSecondary.copy(alpha = 0.5f),
                                modifier = Modifier.size(25.dp)
                            )
                        }
                    } else {
                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = AppPalette.Error.copy(alpha = 0.7f),
                                modifier = Modifier.size(25.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    deleteFutureLessons: Boolean,
    onDeleteFutureLessonsChange: (Boolean) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.action_delete_confirm_title))
        },
        text = {
            Column {
                Text(
                    stringResource(R.string.action_delete_confirm_message),
                    color = AppPalette.TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onDeleteFutureLessonsChange(!deleteFutureLessons) }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = deleteFutureLessons,
                        onCheckedChange = onDeleteFutureLessonsChange
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.lesson_delete_future_duplicates),
                        color = AppPalette.TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = AppPalette.Error)
            ) {
                Text(stringResource(R.string.action_delete))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = AppPalette.TextPrimary)
            ) {
                Text(stringResource(R.string.action_cancel))
            }
        },
        containerColor = AppPalette.Surface,
        titleContentColor = AppPalette.TextPrimary,
        tonalElevation = 6.dp
    )
}
