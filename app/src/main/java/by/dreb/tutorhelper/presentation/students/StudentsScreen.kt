package by.dreb.tutorhelper.presentation.students

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import by.dreb.tutorhelper.R
import by.dreb.tutorhelper.domain.model.Student
import by.dreb.tutorhelper.ui.components.TutorHelperEmptyState
import by.dreb.tutorhelper.ui.components.TutorHelperFilterChip
import by.dreb.tutorhelper.ui.components.TutorHelperTopAppBar
import by.dreb.tutorhelper.ui.theme.AppPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsScreen(
    onStudentClick: (Long) -> Unit,
    onAddStudentClick: () -> Unit,
    viewModel: StudentsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TutorHelperTopAppBar(title = stringResource(R.string.students_title))
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddStudentClick,
                containerColor = AppPalette.Primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Student")
            }
        },
        containerColor = AppPalette.Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Фильтры
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TutorHelperFilterChip(
                    selected = !state.isArchived,
                    onClick = { viewModel.toggleArchive(false) },
                    label = stringResource(R.string.students_active) + " (${state.activeCount})",
                    icon = Icons.Default.CheckCircle
                )
                Spacer(modifier = Modifier.width(12.dp))
                TutorHelperFilterChip(
                    selected = state.isArchived,
                    onClick = { viewModel.toggleArchive(true) },
                    label = stringResource(R.string.students_archived) + " (${state.archivedCount})",
                    icon = Icons.Default.Archive
                )
            }

            // Поиск
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                value = state.query,
                onValueChange = viewModel::updateQuery,
                placeholder = { Text(stringResource(R.string.students_search)) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = AppPalette.TextSecondary
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = AppPalette.Surface,
                    unfocusedContainerColor = AppPalette.Surface,
                    focusedBorderColor = AppPalette.Primary,
                    unfocusedBorderColor = AppPalette.Outline
                ),
                singleLine = true
            )

            if (state.students.isEmpty()) {
                TutorHelperEmptyState(
                    message = stringResource(
                        if (state.isArchived) R.string.students_empty_archived else R.string.students_empty_active
                    ),
                    icon = Icons.Default.Person,
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.students) { student ->
                        ModernStudentCard(
                            student = student,
                            onStudentClick = { onStudentClick(student.id) },
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

@Composable
private fun ModernStudentCard(
    student: Student,
    onStudentClick: () -> Unit,
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
            text = { Text(stringResource(R.string.student_archive_confirm_message), color = AppPalette.TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    onArchiveToggle(true)
                    showArchiveDialog = false
                }) {
                    Text(stringResource(R.string.action_ok), color = AppPalette.Primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showArchiveDialog = false }) {
                    Text(stringResource(R.string.action_cancel), color = AppPalette.TextPrimary)
                }
            },
            containerColor = AppPalette.Surface
        )
    }

    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text(stringResource(R.string.student_restore_confirm_title)) },
            text = { Text(stringResource(R.string.student_restore_confirm_message), color = AppPalette.TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    onArchiveToggle(false)
                    showRestoreDialog = false
                }) {
                    Text(stringResource(R.string.action_ok), color = AppPalette.Primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text(stringResource(R.string.action_cancel), color = AppPalette.TextPrimary)
                }
            },
            containerColor = AppPalette.Surface
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.student_delete_confirm_title)) },
            text = { Text(stringResource(R.string.student_delete_confirm_message), color = AppPalette.TextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = AppPalette.Error)
                ) {
                    Text(stringResource(R.string.action_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.action_cancel), color = AppPalette.TextPrimary)
                }
            },
            containerColor = AppPalette.Surface
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp)
            .border(1.dp, AppPalette.Outline, AppPalette.CardShape)
            .shadow(2.dp, AppPalette.CardShape)
            .clip(AppPalette.CardShape)
            .clickable(onClick = onStudentClick),
        colors = CardDefaults.cardColors(containerColor = AppPalette.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = AppPalette.CardElevation),
        shape = AppPalette.CardShape
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(if (student.isArchived) AppPalette.TextSecondary else AppPalette.Primary)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppPalette.TextPrimary
                    )
                    student.phone?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppPalette.TextSecondary
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
                            tint = AppPalette.Primary.copy(alpha = 0.7f)
                        )
                    }
                    if (student.isArchived) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = AppPalette.Error.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}
