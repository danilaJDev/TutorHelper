package by.dreb.tutorhelper.presentation.schedule

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class LessonEditViewModel @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val studentRepository: StudentRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    val lessonId: Long = checkNotNull(savedStateHandle["lessonId"])

    val students = studentRepository.observeStudents(false)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lessonDetails = lessonRepository.observeLessonDetailsById(lessonId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun updateLesson(
        studentId: Long,
        startTime: LocalDateTime,
        durationMinutes: Int,
        price: Double,
        note: String?
    ) {
        viewModelScope.launch {
            val current = lessonDetails.value?.lesson ?: return@launch
            lessonRepository.upsertLesson(current.copy(
                studentId = studentId,
                startTime = startTime,
                durationMinutes = durationMinutes,
                price = price,
                note = note
            ))
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

    var selectedStudent by remember { mutableStateOf<Student?>(null) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var startTime by remember { mutableStateOf(LocalTime.of(12, 0)) }
    var endTime by remember { mutableStateOf(LocalTime.of(13, 0)) }
    var price by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    LaunchedEffect(details) {
        details?.let {
            selectedStudent = it.student
            selectedDate = it.lesson.startTime.toLocalDate()
            startTime = it.lesson.startTime.toLocalTime()
            endTime = it.lesson.startTime.toLocalTime().plusMinutes(it.lesson.durationMinutes.toLong())
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
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Text(
                    text = "Редактирование занятия",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    modifier = Modifier.padding(start = 8.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = {
                        selectedStudent?.let { student ->
                            val startDateTime = LocalDateTime.of(selectedDate, startTime)
                            val duration = java.time.Duration.between(startTime, endTime).toMinutes().toInt()
                            viewModel.updateLesson(
                                studentId = student.id,
                                startTime = startDateTime,
                                durationMinutes = if (duration > 0) duration else 60,
                                price = price.toDoubleOrNull() ?: student.defaultPrice,
                                note = note.ifBlank { null }
                            )
                            onBackClick()
                        }
                    },
                    enabled = selectedStudent != null
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = if (selectedStudent != null) MaterialTheme.colorScheme.primary else Color.Gray
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Student Selector
                    Box {
                        OutlinedTextField(
                            value = selectedStudent?.name ?: stringResource(R.string.lesson_label_student),
                            onValueChange = {},
                            label = { Text(stringResource(R.string.lesson_label_student)) },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            trailingIcon = {
                                Icon(Icons.Default.ArrowDropDown, null, Modifier.clickable { studentDropdownExpanded = true })
                            }
                        )
                        DropdownMenu(
                            expanded = studentDropdownExpanded,
                            onDismissRequest = { studentDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
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
                        Box(modifier = Modifier.matchParentSize().clickable { studentDropdownExpanded = true })
                    }

                    // Date Selector
                    OutlinedTextField(
                        value = selectedDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                        onValueChange = {},
                        label = { Text(stringResource(R.string.lesson_label_date)) },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, null, Modifier.clickable { showDatePicker = true })
                        }
                    )
                    Box(modifier = Modifier.fillMaxWidth().height(56.dp).clickable { showDatePicker = true })

                    // Time Selectors
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = startTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                            onValueChange = {},
                            label = { Text(stringResource(R.string.lesson_label_start)) },
                            modifier = Modifier.weight(1f).clickable { showStartTimePicker = true },
                            readOnly = true
                        )
                        OutlinedTextField(
                            value = endTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                            onValueChange = {},
                            label = { Text(stringResource(R.string.lesson_label_end)) },
                            modifier = Modifier.weight(1f).clickable { showEndTimePicker = true },
                            readOnly = true
                        )
                    }

                    // Price Field
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text(stringResource(R.string.lesson_label_price)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Note Field
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text(stringResource(R.string.lesson_label_note)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    // Dialogs
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            selectableDates = object : androidx.compose.material3.SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val today = LocalDate.now().atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
                    return utcTimeMillis >= today
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        selectedDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text(stringResource(R.string.action_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.action_cancel_alt)) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showStartTimePicker) {
        val timePickerState = rememberTimePickerState(initialHour = startTime.hour, initialMinute = startTime.minute)
        TimePickerDialog(
            onDismissRequest = { showStartTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    showStartTimePicker = false
                }) { Text(stringResource(R.string.action_ok)) }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }

    if (showEndTimePicker) {
        val timePickerState = rememberTimePickerState(initialHour = endTime.hour, initialMinute = endTime.minute)
        TimePickerDialog(
            onDismissRequest = { showEndTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    endTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    showEndTimePicker = false
                }) { Text(stringResource(R.string.action_ok)) }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
}
