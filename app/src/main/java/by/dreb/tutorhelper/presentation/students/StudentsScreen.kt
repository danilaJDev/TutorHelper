package by.dreb.tutorhelper.presentation.students

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import by.dreb.tutorhelper.R
import by.dreb.tutorhelper.domain.model.Student

@Composable
fun StudentsScreen(viewModel: StudentsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = stringResource(R.string.students_title),
            style = MaterialTheme.typography.headlineSmall
        )
        SegmentedButtonRow(modifier = Modifier.padding(top = 12.dp)) {
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
            modifier = Modifier.padding(top = 12.dp),
            value = state.query,
            onValueChange = viewModel::updateQuery,
            label = { Text(stringResource(R.string.students_search)) }
        )

        LazyColumn(
            modifier = Modifier.padding(top = 12.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.students) { student ->
                StudentCard(student)
            }
        }
    }
}

@Composable
private fun StudentCard(student: Student) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
    }
}
