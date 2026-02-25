package by.dreb.tutorhelper.presentation.schedule

import android.content.res.Configuration
import android.os.LocaleList
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.dreb.tutorhelper.R
import by.dreb.tutorhelper.domain.model.Student
import by.dreb.tutorhelper.domain.repository.LessonRepository
import by.dreb.tutorhelper.domain.repository.StudentRepository
import by.dreb.tutorhelper.presentation.components.FormCardSection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class LessonEditViewModel @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val studentRepository: StudentRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    val lessonId: Long = savedStateHandle.get<Long>("lessonId") ?: -1L

    val students = studentRepository.observeStudents(false)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isSaved = MutableStateFlow(false)
    val isSaved = _isSaved.asStateFlow()

    private val _priceError = MutableStateFlow<Int?>(null)
    val priceError = _priceError.asStateFlow()

    val lessonDetails = if (lessonId > 0) {
        lessonRepository.observeLessonDetailsById(lessonId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    } else {
        kotlinx.coroutines.flow.flowOf(null)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    }

    fun clearErrors() {
        _priceError.value = null
    }

    fun updateLesson(
        studentId: Long,
        startTime: LocalDateTime,
        durationMinutes: Int,
        priceStr: String,
        note: String?
    ) {
        val price = priceStr.toDoubleOrNull()
        if (price == null) {
            _priceError.value = R.string.error_invalid_price
            return
        }
        _priceError.value = null

        viewModelScope.launch {
            val current = lessonDetails.value?.lesson ?: return@launch
            val dateChanged = current.startTime.toLocalDate() != startTime.toLocalDate()
            val result = runCatching {
                lessonRepository.upsertLesson(
                    current.copy(
                        studentId = studentId,
                        startTime = startTime,
                        durationMinutes = durationMinutes,
                        price = price,
                        note = note,
                        isCompleted = if (dateChanged) false else current.isCompleted
                    )
                )
            }
            if (result.isSuccess) {
                _isSaved.value = true
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonEditScreen(
    lessonId: Long,
    onBackClick: () -> Unit,
    viewModel: LessonEditViewModel = hiltViewModel()
) {
    val details by viewModel.lessonDetails.collectAsState()
    val students by viewModel.students.collectAsState()
    val isSaved by viewModel.isSaved.collectAsState()
    val priceError by viewModel.priceError.collectAsState()

    var selectedStudent by remember { mutableStateOf<Student?>(null) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var startTime by remember { mutableStateOf(LocalTime.of(12, 0)) }
    var endTime by remember { mutableStateOf(LocalTime.of(13, 0)) }
    var price by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    val calendarLocale = remember {
        Locale.Builder().setLanguage("ru").setRegion("BY").build()
    }
    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", calendarLocale)
    }
    val leadingIconColors = OutlinedTextFieldDefaults.colors(
        focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
        unfocusedLeadingIconColor = MaterialTheme.colorScheme.primary,
        disabledLeadingIconColor = MaterialTheme.colorScheme.primary,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline
    )

    LaunchedEffect(isSaved) {
        if (isSaved) {
            onBackClick()
        }
    }

    LaunchedEffect(details) {
        details?.let {
            selectedStudent = it.student
            selectedDate = it.lesson.startTime.toLocalDate()
            startTime = it.lesson.startTime.toLocalTime()
            endTime =
                it.lesson.startTime.toLocalTime().plusMinutes(it.lesson.durationMinutes.toLong())
            price = it.lesson.price.toString()
            note = it.lesson.note ?: ""
        }
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
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
                        contentDescription = stringResource(R.string.action_back)
                    )
                }
                Text(
                    text = stringResource(R.string.action_edit),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                )
                TextButton(
                    onClick = {
                        selectedStudent?.let { student ->
                            val startDateTime = LocalDateTime.of(selectedDate, startTime)
                            val duration =
                                java.time.Duration.between(startTime, endTime).toMinutes().toInt()
                            viewModel.updateLesson(
                                studentId = student.id,
                                startTime = startDateTime,
                                durationMinutes = if (duration > 0) duration else 60,
                                priceStr = price,
                                note = note.ifBlank { null }
                            )
                        }
                    },
                    enabled = selectedStudent != null && endTime.isAfter(startTime)
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FormCardSection {
                ExposedDropdownMenuBox(
                    expanded = studentDropdownExpanded,
                    onExpandedChange = { studentDropdownExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedStudent?.name ?: "",
                        onValueChange = {},
                        label = { Text(stringResource(R.string.lesson_label_student)) },
                        placeholder = { Text(stringResource(R.string.lesson_label_student)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(
                                ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                                enabled = true
                            ),
                        readOnly = true,
                        leadingIcon = { Icon(Icons.Default.Person, null) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = studentDropdownExpanded)
                        },
                        colors = leadingIconColors,
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = studentDropdownExpanded,
                        onDismissRequest = { studentDropdownExpanded = false }
                    ) {
                        if (students.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.students_empty_active)) },
                                onClick = { studentDropdownExpanded = false }
                            )
                        } else {
                            students.forEach { student ->
                                DropdownMenuItem(
                                    text = { Text(student.name) },
                                    onClick = {
                                        selectedStudent = student
                                        studentDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            FormCardSection(title = "Время проведения") {
                EditClickableField(
                    value = selectedDate.format(dateFormatter),
                    label = stringResource(R.string.lesson_label_date),
                    icon = Icons.Default.CalendarMonth,
                    onClick = { showDatePicker = true }
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    EditClickableField(
                        value = startTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                        label = stringResource(R.string.lesson_label_start),
                        icon = Icons.Default.AccessTime,
                        modifier = Modifier.weight(1f),
                        onClick = { showStartTimePicker = true }
                    )
                    EditClickableField(
                        value = endTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                        label = stringResource(R.string.lesson_label_end),
                        icon = Icons.Default.AccessTime,
                        modifier = Modifier.weight(1f),
                        isError = !endTime.isAfter(startTime),
                        onClick = { showEndTimePicker = true }
                    )
                }

                if (!endTime.isAfter(startTime)) {
                    Text(
                        text = stringResource(R.string.error_time_range),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
            }

            FormCardSection {
                OutlinedTextField(
                    value = price,
                    onValueChange = {
                        price = it
                        if (priceError != null) viewModel.clearErrors()
                    },
                    label = { Text(stringResource(R.string.lesson_label_price)) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.AttachMoney, null) },
                    colors = leadingIconColors,
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    isError = priceError != null,
                    supportingText = priceError?.let { { Text(stringResource(it)) } }
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(stringResource(R.string.lesson_label_note)) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Description, null) },
                    colors = leadingIconColors,
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault())
                .toInstant().toEpochMilli()
        )
        LocalizedDatePickerDialog(
            state = datePickerState,
            locale = calendarLocale,
            onDismissRequest = { showDatePicker = false },
            onConfirm = {
                datePickerState.selectedDateMillis?.let {
                    selectedDate =
                        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                }
                showDatePicker = false
            }
        )
    }

    if (showStartTimePicker) {
        val timePickerState =
            rememberTimePickerState(
                initialHour = startTime.hour,
                initialMinute = startTime.minute,
                is24Hour = true
            )
        EditTimePickerDialog(
            onDismissRequest = { showStartTimePicker = false },
            confirmButton = {
                DialogActionButton(text = "ОК", onClick = {
                    startTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    showStartTimePicker = false
                })
            },
            dismissButton = {
                DialogActionButton(text = "Отмена", onClick = { showStartTimePicker = false })
            }
        ) {
            TimePicker(
                state = timePickerState,
                colors = TimePickerDefaults.colors(containerColor = Color.White)
            )
        }
    }

    if (showEndTimePicker) {
        val timePickerState =
            rememberTimePickerState(
                initialHour = endTime.hour,
                initialMinute = endTime.minute,
                is24Hour = true
            )
        EditTimePickerDialog(
            onDismissRequest = { showEndTimePicker = false },
            confirmButton = {
                DialogActionButton(text = "ОК", onClick = {
                    endTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    showEndTimePicker = false
                })
            },
            dismissButton = {
                DialogActionButton(text = "Отмена", onClick = { showEndTimePicker = false })
            }
        ) {
            TimePicker(
                state = timePickerState,
                colors = TimePickerDefaults.colors(containerColor = Color.White)
            )
        }
    }
}

@Composable
private fun EditClickableField(
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
            .clickable { onClick() },
        readOnly = true,
        enabled = false,
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
private fun EditTimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismissRequest) {
        androidx.compose.material3.Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 6.dp,
            modifier = Modifier
                .width(320.dp)
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                content()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        dismissButton()
                        confirmButton()
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Button(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            .width(120.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Text(text = text, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocalizedDatePickerDialog(
    state: androidx.compose.material3.DatePickerState,
    locale: Locale,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit
) {
    LocalizedContent(locale) {
        DatePickerDialog(
            onDismissRequest = onDismissRequest,
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text("ОК", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRequest) {
                    Text("Отмена")
                }
            }
        ) {
            DatePicker(
                state = state,
                colors = androidx.compose.material3.DatePickerDefaults.colors(
                    containerColor = Color.White
                )
            )
        }
    }
}

@Composable
private fun LocalizedContent(
    locale: Locale,
    content: @Composable () -> Unit
) {
    val baseContext = LocalContext.current
    val configuration = remember(locale) {
        Configuration(baseContext.resources.configuration).apply {
            setLocales(LocaleList(locale))
        }
    }
    val localizedContext = remember(locale) {
        baseContext.createConfigurationContext(configuration)
    }
    DisposableEffect(locale) {
        val previousLocale = Locale.getDefault()
        Locale.setDefault(locale)
        onDispose {
            Locale.setDefault(previousLocale)
        }
    }

    androidx.compose.runtime.CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides configuration,
        content = content
    )
}
