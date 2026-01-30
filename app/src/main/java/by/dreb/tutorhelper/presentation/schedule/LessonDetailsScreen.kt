package by.dreb.tutorhelper.presentation.schedule

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.dreb.tutorhelper.R
import by.dreb.tutorhelper.domain.model.LessonDetails
import by.dreb.tutorhelper.domain.repository.LessonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class LessonDetailsViewModel @Inject constructor(
    private val lessonRepository: LessonRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val lessonId: Long = checkNotNull(savedStateHandle["lessonId"])

    val lessonDetails: StateFlow<LessonDetails?> = lessonRepository.observeLessonDetailsById(lessonId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}

@Composable
fun LessonDetailsScreen(
    lessonId: Long,
    onBackClick: () -> Unit,
    onEditClick: (Long) -> Unit,
    viewModel: LessonDetailsViewModel = hiltViewModel()
) {
    val lessonDetails by viewModel.lessonDetails.collectAsState()

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
                    text = stringResource(R.string.action_back),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    modifier = Modifier.padding(start = 8.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { onEditClick(lessonId) }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
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
        ) {
            lessonDetails?.let { details ->
                val lesson = details.lesson
                val student = details.student
                val dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("ru"))
                val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                val endTime = lesson.startTime.plusMinutes(lesson.durationMinutes.toLong())

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        DetailItem(
                            icon = Icons.Default.Person,
                            label = stringResource(R.string.lesson_label_student),
                            value = student.name
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        DetailItem(
                            icon = Icons.Default.Event,
                            label = stringResource(R.string.lesson_label_date),
                            value = lesson.startTime.format(dateFormatter)
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        DetailItem(
                            icon = Icons.Default.Schedule,
                            label = stringResource(R.string.lesson_label_time),
                            value = "${lesson.startTime.format(timeFormatter)} - ${endTime.format(timeFormatter)} (${lesson.durationMinutes} мин)"
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        DetailItem(
                            icon = Icons.Default.Payments,
                            label = stringResource(R.string.finance_payment_amount).replace(": %1$.2f", ""),
                            value = "${lesson.price} ${stringResource(R.string.currency_rub)}"
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        DetailItem(
                            icon = Icons.Default.Notes,
                            label = stringResource(R.string.lesson_label_note),
                            value = lesson.note?.takeIf { it.isNotBlank() } ?: stringResource(R.string.field_not_filled)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp
                )
            )
        }
    }
}
