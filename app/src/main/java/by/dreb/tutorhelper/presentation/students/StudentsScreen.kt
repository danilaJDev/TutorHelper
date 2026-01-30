package by.dreb.tutorhelper.presentation.students

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import by.dreb.tutorhelper.R
import by.dreb.tutorhelper.domain.model.Student
import by.dreb.tutorhelper.ui.components.MainContentCard
import by.dreb.tutorhelper.ui.components.TutorHelperHeader
import by.dreb.tutorhelper.ui.theme.StatusGreen
import by.dreb.tutorhelper.ui.theme.StatusOnGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsScreen(
    onStudentClick: (Long) -> Unit,
    onAddStudentClick: () -> Unit,
    viewModel: StudentsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TutorHelperHeader(
            title = stringResource(R.string.students_title),
            actionIcon = Icons.Default.Add,
            onActionClick = onAddStudentClick
        )

        MainContentCard(modifier = Modifier.weight(1f)) {
            Column(modifier = Modifier.padding(16.dp)) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = !state.isArchived,
                        onClick = { viewModel.toggleArchive(false) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        label = { Text(text = stringResource(R.string.students_active) + " (${state.activeCount})") }
                    )
                    SegmentedButton(
                        selected = state.isArchived,
                        onClick = { viewModel.toggleArchive(true) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        label = { Text(text = stringResource(R.string.students_archived) + " (${state.archivedCount})") }
                    )
                }

                OutlinedTextField(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth(),
                    value = state.query,
                    onValueChange = viewModel::updateQuery,
                    label = { Text(stringResource(R.string.students_search)) },
                    shape = MaterialTheme.shapes.medium
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (state.students.isEmpty()) {
                    Text(
                        text = stringResource(
                            if (state.isArchived) R.string.students_empty_archived else R.string.students_empty_active
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.students) { student ->
                            StudentCard(
                                student = student,
                                onStudentClick = onStudentClick,
                                onArchiveToggle = { archived ->
                                    viewModel.setStudentArchived(student.id, archived)
                                },
                                onDeleteClick = {
                                    viewModel.deleteStudent(student.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentCard(
    student: Student,
    onStudentClick: (Long) -> Unit,
    onArchiveToggle: (Boolean) -> Unit,
    onDeleteClick: () -> Unit
) {
    var showArchiveDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showArchiveDialog) {
        AlertDialog(
            onDismissRequest = { showArchiveDialog = false },
            title = { Text(stringResource(R.string.student_archive_confirm_title)) },
            text = { Text(stringResource(R.string.student_archive_confirm_message)) },
            confirmButton = {
                TextButton(onClick = {
                    onArchiveToggle(true)
                    showArchiveDialog = false
                }) {
                    Text(stringResource(R.string.action_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showArchiveDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text(stringResource(R.string.student_restore_confirm_title)) },
            text = { Text(stringResource(R.string.student_restore_confirm_message)) },
            confirmButton = {
                TextButton(onClick = {
                    onArchiveToggle(false)
                    showRestoreDialog = false
                }) {
                    Text(stringResource(R.string.action_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.student_delete_confirm_title)) },
            text = { Text(stringResource(R.string.student_delete_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.action_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onStudentClick(student.id) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                student.phone?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                student.note?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
            Row {
                IconButton(
                    onClick = {
                        if (student.isArchived) {
                            showRestoreDialog = true
                        } else {
                            showArchiveDialog = true
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (student.isArchived) Icons.Default.Unarchive else Icons.Default.Archive,
                        contentDescription = null,
                        tint = if (student.isArchived) StatusOnGreen else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (student.isArchived) {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
