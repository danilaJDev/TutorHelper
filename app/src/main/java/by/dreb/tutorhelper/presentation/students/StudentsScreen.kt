package by.dreb.tutorhelper.presentation.students

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import by.dreb.tutorhelper.R
import by.dreb.tutorhelper.domain.model.Student
import androidx.compose.foundation.shape.RoundedCornerShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsScreen(viewModel: StudentsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            color = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.students_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(
                        onClick = { viewModel.toggleArchive(false) },
                        label = { Text(text = stringResource(R.string.students_active_count, state.activeCount)) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f),
                            labelColor = MaterialTheme.colorScheme.onPrimary,
                            leadingIconContentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                    AssistChip(
                        onClick = { viewModel.toggleArchive(true) },
                        label = { Text(text = stringResource(R.string.students_archived_count, state.archivedCount)) },
                        leadingIcon = { Icon(Icons.Default.Archive, contentDescription = null) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f),
                            labelColor = MaterialTheme.colorScheme.onPrimary,
                            leadingIconContentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.padding(top = 4.dp)) {
                SegmentedButton(
                    selected = !state.isArchived,
                    onClick = { viewModel.toggleArchive(false) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Text(text = stringResource(R.string.students_active))
                }
                SegmentedButton(
                    selected = state.isArchived,
                    onClick = { viewModel.toggleArchive(true) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Text(text = stringResource(R.string.students_archived))
                }
            }

            OutlinedTextField(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth(),
                value = state.query,
                onValueChange = viewModel::updateQuery,
                label = { Text(stringResource(R.string.students_search)) }
            )
        }

        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 24.dp, top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.students.isEmpty()) {
                item {
                    Text(
                        text = stringResource(
                            if (state.isArchived) R.string.students_empty_archived else R.string.students_empty_active
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }
            items(state.students) { student ->
                StudentCard(
                    student = student,
                    onArchiveToggle = { archived ->
                        viewModel.setStudentArchived(student.id, archived)
                    }
                )
            }
        }
    }
}

@Composable
private fun StudentCard(student: Student, onArchiveToggle: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = student.name, style = MaterialTheme.typography.titleMedium)
                    student.phone?.let { Text(text = it, style = MaterialTheme.typography.bodyMedium) }
                    student.note?.let { Text(text = it, style = MaterialTheme.typography.bodySmall) }
                    if (student.isArchived) {
                        Text(
                            text = stringResource(R.string.students_archived_label),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
                TextButton(onClick = { onArchiveToggle(!student.isArchived) }) {
                    androidx.compose.material3.Icon(
                        if (student.isArchived) Icons.Default.Unarchive else Icons.Default.Archive,
                        contentDescription = null
                    )
                    Text(
                        text = stringResource(
                            if (student.isArchived) R.string.students_restore else R.string.students_archive
                        ),
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }
        }
    }
}
