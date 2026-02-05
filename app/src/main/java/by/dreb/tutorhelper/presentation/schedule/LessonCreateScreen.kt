package by.dreb.tutorhelper.presentation.schedule

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.dreb.tutorhelper.domain.model.Lesson
import by.dreb.tutorhelper.domain.model.Student
import by.dreb.tutorhelper.domain.repository.LessonRepository
import by.dreb.tutorhelper.domain.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

// --- State Definition ---

data class LessonCreateUiState(
    val selectedStudent: Student? = null,
    val date: LocalDate = LocalDate.now(),
    val startTime: LocalTime = LocalTime.of(12, 0),
    val endTime: LocalTime = LocalTime.of(13, 0),
    val price: String = "",
    val note: String = "",
    val isDuplicate: Boolean = false,
    val duplicateUntil: LocalDate = LocalDate.now().plusMonths(1),
    val isLoading: Boolean = false,
    val isSaved: Boolean = false
) {
    val isValid: Boolean
        get() = selectedStudent != null &&
                price.isNotBlank() &&
                price.toDoubleOrNull() != null &&
                endTime.isAfter(startTime)
}

// --- ViewModel ---

@HiltViewModel
class LessonCreateViewModel @Inject constructor(
    private val lessonRepository: LessonRepository,
    studentRepository: StudentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LessonCreateUiState())
    val uiState = _uiState.asStateFlow()

    // Объединяем список учеников и UI state, чтобы удобно использовать в Composable
    val studentsState = studentRepository.observeStudents(false)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onStudentSelected(student: Student) {
        _uiState.update {
            it.copy(
                selectedStudent = student,
                price = student.defaultPrice.toString() // Автозаполнение цены
            )
        }
    }

    fun onDateChanged(date: LocalDate) {
        _uiState.update { it.copy(date = date) }
    }

    fun onStartTimeChanged(time: LocalTime) {
        _uiState.update { currentState ->
            // При изменении времени начала, сохраняем длительность урока (сдвигаем конец)
            val duration = Duration.between(currentState.startTime, currentState.endTime)
            val newEndTime = time.plus(duration)
            currentState.copy(startTime = time, endTime = newEndTime)
        }
    }

    fun onEndTimeChanged(time: LocalTime) {
        _uiState.update { it.copy(endTime = time) }
    }

    fun onPriceChanged(price: String) {
        // Разрешаем только цифры и одну точку
        if (price.count { it == '.' } <= 1 && price.replace(".", "").all { it.isDigit() }) {
            _uiState.update { it.copy(price = price) }
        }
    }

    fun onNoteChanged(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun onDuplicateChanged(isDuplicate: Boolean) {
        _uiState.update { it.copy(isDuplicate = isDuplicate) }
    }

    fun onDuplicateUntilChanged(date: LocalDate) {
        _uiState.update { it.copy(duplicateUntil = date) }
    }

    fun saveLesson() {
        val currentState = _uiState.value
        val student = currentState.selectedStudent ?: return

        if (!currentState.isValid) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val startDateTime = LocalDateTime.of(currentState.date, currentState.startTime)
            val durationMinutes = Duration.between(currentState.startTime, currentState.endTime).toMinutes().toInt()

            val baseLesson = Lesson(
                id = 0,
                studentId = student.id,
                subject = "Занятие", // Можно вынести в UI, если нужно менять тему
                startTime = startDateTime,
                durationMinutes = if (durationMinutes > 0) durationMinutes else 60,
                price = currentState.price.toDoubleOrNull() ?: 0.0,
                note = currentState.note.ifBlank { null }
            )

            lessonRepository.upsertLesson(baseLesson)

            if (currentState.isDuplicate) {
                var currentStartTime = startDateTime.plusWeeks(1)
                while (!currentStartTime.toLocalDate().isAfter(currentState.duplicateUntil)) {
                    lessonRepository.upsertLesson(baseLesson.copy(startTime = currentStartTime))
                    currentStartTime = currentStartTime.plusWeeks(1)
                }
            }

            _uiState.update { it.copy(isLoading = false, isSaved = true) }
        }
    }
}

// --- Screen ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonCreateScreen(
    onBackClick: () -> Unit,
    viewModel: LessonCreateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val students by viewModel.studentsState.collectAsState()

    // Если сохранение прошло успешно, выходим
    if (uiState.isSaved) {
        androidx.compose.runtime.LaunchedEffect(Unit) {
            onBackClick()
        }
    }

    // Состояния диалогов
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var showDuplicateUntilPicker by remember { mutableStateOf(false) }
    var studentDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад"
                    )
                }
                Text(
                    text = "Новое занятие",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                )
                TextButton(
                    onClick = viewModel::saveLesson,
                    enabled = uiState.isValid && !uiState.isLoading
                ) {
                    Text(
                        text = "Сохранить",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Блок выбора ученика
            CardSection {
                ExposedDropdownMenuBox(
                    expanded = studentDropdownExpanded,
                    onExpandedChange = { studentDropdownExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = uiState.selectedStudent?.name ?: "",
                        onValueChange = {},
                        label = { Text("Ученик") },
                        placeholder = { Text("Выберите ученика") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        readOnly = true,
                        leadingIcon = {
                            Icon(Icons.Default.Person, null)
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = studentDropdownExpanded)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = studentDropdownExpanded,
                        onDismissRequest = { studentDropdownExpanded = false }
                    ) {
                        if (students.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Нет активных учеников") },
                                onClick = { studentDropdownExpanded = false }
                            )
                        } else {
                            students.forEach { student ->
                                DropdownMenuItem(
                                    text = { Text(student.name) },
                                    onClick = {
                                        viewModel.onStudentSelected(student)
                                        studentDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 2. Блок времени и даты
            CardSection(title = "Время проведения") {
                // Дата
                ClickableField(
                    value = uiState.date.format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy")),
                    label = "Дата",
                    icon = Icons.Default.CalendarMonth,
                    onClick = { showDatePicker = true }
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Время начала
                    ClickableField(
                        value = uiState.startTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                        label = "Начало",
                        icon = Icons.Default.AccessTime,
                        modifier = Modifier.weight(1f),
                        onClick = { showStartTimePicker = true }
                    )
                    // Время окончания
                    ClickableField(
                        value = uiState.endTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                        label = "Конец",
                        icon = Icons.Default.AccessTime,
                        modifier = Modifier.weight(1f),
                        isError = !uiState.endTime.isAfter(uiState.startTime),
                        onClick = { showEndTimePicker = true }
                    )
                }

                if (!uiState.endTime.isAfter(uiState.startTime)) {
                    Text(
                        text = "Время окончания должно быть позже начала",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
            }

            // 3. Блок финансов и заметок
            CardSection {
                OutlinedTextField(
                    value = uiState.price,
                    onValueChange = viewModel::onPriceChanged,
                    label = { Text("Цена") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.AttachMoney, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = uiState.note,
                    onValueChange = viewModel::onNoteChanged,
                    label = { Text("Заметка") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Description, null) },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )
            }

            // 4. Блок повторения
            CardSection {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { viewModel.onDuplicateChanged(!uiState.isDuplicate) },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Повторение",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Повторять каждую неделю",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = uiState.isDuplicate,
                        onCheckedChange = viewModel::onDuplicateChanged
                    )
                }

                AnimatedVisibility(
                    visible = uiState.isDuplicate,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(modifier = Modifier.padding(top = 16.dp)) {
                        HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))
                        ClickableField(
                            value = uiState.duplicateUntil.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                            label = "Повторять до",
                            icon = Icons.Default.CalendarMonth,
                            onClick = { showDuplicateUntilPicker = true }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(50.dp))
        }
    }

    // --- Dialogs ---

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.date.atStartOfDay(ZoneId.systemDefault())
                .toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        viewModel.onDateChanged(
                            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                        )
                    }
                    showDatePicker = false
                }) { Text("ОК") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Отмена")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showDuplicateUntilPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.duplicateUntil.atStartOfDay(ZoneId.systemDefault())
                .toInstant().toEpochMilli(),
            selectableDates = object : androidx.compose.material3.SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    // Разрешаем только даты в будущем
                    return utcTimeMillis >= System.currentTimeMillis()
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDuplicateUntilPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        viewModel.onDuplicateUntilChanged(
                            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                        )
                    }
                    showDuplicateUntilPicker = false
                }) { Text("ОК") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showStartTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = uiState.startTime.hour,
            initialMinute = uiState.startTime.minute
        )
        TimePickerDialog(
            onDismissRequest = { showStartTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onStartTimeChanged(
                        LocalTime.of(timePickerState.hour, timePickerState.minute)
                    )
                    showStartTimePicker = false
                }) { Text("ОК") }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }

    if (showEndTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = uiState.endTime.hour,
            initialMinute = uiState.endTime.minute
        )
        TimePickerDialog(
            onDismissRequest = { showEndTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onEndTimeChanged(
                        LocalTime.of(timePickerState.hour, timePickerState.minute)
                    )
                    showEndTimePicker = false
                }) { Text("ОК") }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
}

// --- Helper Composables ---

@Composable
fun CardSection(
    title: String? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp) // Легкая тень
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            content()
        }
    }
}

@Composable
fun ClickableField(
    value: String,
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        label = { Text(label) },
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }, // Обработка клика по всему полю
        readOnly = true,
        enabled = false, // Отключаем стандартный ввод, но оставляем кликабельность через Box/Modifier
        leadingIcon = { Icon(icon, contentDescription = null) },
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
            disabledLabelColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            disabledLeadingIconColor = MaterialTheme.colorScheme.primary
        ),
        shape = RoundedCornerShape(12.dp)
    )
}


@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismissRequest) {
        androidx.compose.material3.Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier
                .width(320.dp) // Стандартная ширина для диалогов
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                content()
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    confirmButton()
                }
            }
        }
    }
}