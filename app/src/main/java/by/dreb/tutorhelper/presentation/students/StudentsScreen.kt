package by.dreb.tutorhelper.presentation.students

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import by.dreb.tutorhelper.R
import by.dreb.tutorhelper.domain.model.Student
import by.dreb.tutorhelper.presentation.common.AppPalette

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
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.students_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = AppPalette.TextPrimary
                    )
                },
                windowInsets = WindowInsets(top = 0.dp),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppPalette.Background,
                    scrolledContainerColor = AppPalette.Surface.copy(alpha = 0.95f)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddStudentClick,
                containerColor = AppPalette.Primary,
                contentColor = Color.White,
                shape = AppPalette.CardShape,
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        },
        containerColor = AppPalette.Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StudentsFilterChip(
                    selected = !state.isArchived,
                    onClick = { viewModel.toggleArchive(false) },
                    label = stringResource(R.string.students_active) + " (${state.activeCount})",
                    icon = Icons.Default.CheckCircle
                )
                Spacer(modifier = Modifier.width(12.dp))
                StudentsFilterChip(
                    selected = state.isArchived,
                    onClick = { viewModel.toggleArchive(true) },
                    label = stringResource(R.string.students_archived) + " (${state.archivedCount})",
                    icon = Icons.Default.Archive
                )
            }

            OutlinedTextField(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                value = state.query,
                onValueChange = viewModel::updateQuery,
                label = { Text(stringResource(R.string.students_search)) },
                shape = AppPalette.CardShape,
                singleLine = true
            )

            Box(modifier = Modifier.fillMaxSize()) {
                if (state.students.isEmpty()) {
                    EmptyStudentsState(isArchived = state.isArchived)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp, top = 16.dp),
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
private fun StudentsFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = if (selected) {
            { Icon(icon, null, modifier = Modifier.size(18.dp)) }
        } else null,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = AppPalette.Primary.copy(alpha = 0.15f),
            selectedLabelColor = AppPalette.Primary,
            selectedLeadingIconColor = AppPalette.Primary,
            containerColor = AppPalette.Surface,
            labelColor = AppPalette.TextSecondary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = if (selected) AppPalette.Primary else Color.Transparent,
            selectedBorderColor = AppPalette.Primary
        )
    )
}

@Composable
private fun EmptyStudentsState(isArchived: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Groups,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = AppPalette.TextSecondary.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(
                    if (isArchived) R.string.students_empty_archived else R.string.students_empty_active
                ),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                color = AppPalette.TextSecondary
            )
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
            title = {
                Text(
                    stringResource(R.string.student_archive_confirm_title),
                    color = AppPalette.TextPrimary
                )
            },
            text = {
                Text(
                    stringResource(R.string.student_archive_confirm_message),
                    color = AppPalette.TextSecondary
                )
            },
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
            containerColor = AppPalette.Surface,
            titleContentColor = AppPalette.TextPrimary
        )
    }

    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = {
                Text(
                    stringResource(R.string.student_restore_confirm_title),
                    color = AppPalette.TextPrimary
                )
            },
            text = {
                Text(
                    stringResource(R.string.student_restore_confirm_message),
                    color = AppPalette.TextSecondary
                )
            },
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
            containerColor = AppPalette.Surface,
            titleContentColor = AppPalette.TextPrimary
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    stringResource(R.string.student_delete_confirm_title),
                    color = AppPalette.TextPrimary
                )
            },
            text = {
                Text(
                    stringResource(R.string.student_delete_confirm_message),
                    color = AppPalette.TextSecondary
                )
            },
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
            containerColor = AppPalette.Surface,
            titleContentColor = AppPalette.TextPrimary
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE2E8F0), AppPalette.CardShape)
            .shadow(2.dp, AppPalette.CardShape)
            .clip(AppPalette.CardShape)
            .clickable { onStudentClick(student.id) },
        colors = CardDefaults.cardColors(containerColor = AppPalette.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = AppPalette.CardElevation),
        shape = AppPalette.CardShape
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
                    color = AppPalette.TextPrimary
                )
                student.phone?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppPalette.TextSecondary
                    )
                }
                student.note?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppPalette.TextSecondary,
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
                        tint = if (student.isArchived) AppPalette.Success else AppPalette.TextSecondary
                    )
                }
                if (student.isArchived) {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = AppPalette.Error
                        )
                    }
                }
            }
        }
    }
}
