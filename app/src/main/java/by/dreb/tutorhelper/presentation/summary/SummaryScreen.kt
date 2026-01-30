package by.dreb.tutorhelper.presentation.summary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import by.dreb.tutorhelper.R
import by.dreb.tutorhelper.domain.model.ThemeMode
import by.dreb.tutorhelper.presentation.settings.SettingsUiState
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun SummaryScreen(
    settingsState: SettingsUiState,
    onThemeSelected: (ThemeMode) -> Unit,
    viewModel: SummaryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 1. Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.summary_title),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = androidx.compose.ui.graphics.Color.Black
                )
            )

            // No action button for summary usually, but following the pattern
            IconButton(
                onClick = { /* TODO */ },
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary, androidx.compose.foundation.shape.CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Insights,
                    contentDescription = null,
                    tint = androidx.compose.ui.graphics.Color.White
                )
            }
        }

        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))

        // 2. Combined Block
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = stringResource(R.string.summary_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = androidx.compose.ui.graphics.Color.Gray
                    )
                }
                item { SummaryHeroCard(state.summary) }
                item { SummaryStatsGrid(state.summary) }
                item { InsightsCard(state.summary) }
                item {
                    SettingsCard(
                        settingsState = settingsState,
                        onThemeSelected = onThemeSelected
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryHeroCard(summary: by.dreb.tutorhelper.domain.model.Summary) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = stringResource(R.string.summary_income), style = MaterialTheme.typography.titleMedium)
            Text(
                text = stringResource(R.string.summary_income_total, summary.incomeTotal),
                style = MaterialTheme.typography.headlineSmall
            )
            Text(text = stringResource(R.string.summary_income_hint, summary.paidLessonsCount))
        }
    }
}

@Composable
private fun SummaryStatsGrid(summary: by.dreb.tutorhelper.domain.model.Summary) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryStatCard(
                label = stringResource(R.string.summary_lessons_short),
                value = summary.lessonsCount.toString(),
                icon = Icons.Default.School,
                modifier = Modifier.weight(1f)
            )
            SummaryStatCard(
                label = stringResource(R.string.summary_students_short),
                value = summary.studentsCount.toString(),
                icon = Icons.Default.Groups,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryStatCard(
                label = stringResource(R.string.summary_paid_short),
                value = summary.paidLessonsCount.toString(),
                icon = Icons.Default.Payments,
                modifier = Modifier.weight(1f)
            )
            SummaryStatCard(
                label = stringResource(R.string.summary_archived_short),
                value = summary.archivedStudentsCount.toString(),
                icon = Icons.Default.Insights,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SummaryStatCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                androidx.compose.material3.Icon(icon, contentDescription = null)
                Text(text = label, style = MaterialTheme.typography.bodyMedium)
            }
            Text(text = value, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun InsightsCard(summary: by.dreb.tutorhelper.domain.model.Summary) {
    val insights = listOf(
        stringResource(R.string.summary_insight_lessons, summary.lessonsCount),
        stringResource(R.string.summary_insight_students, summary.studentsCount),
        stringResource(R.string.summary_insight_income, summary.incomeTotal)
    )
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = stringResource(R.string.summary_insights_title), style = MaterialTheme.typography.titleMedium)
            insights.forEach { insight ->
                Text(text = insight, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun SettingsCard(
    settingsState: SettingsUiState,
    onThemeSelected: (ThemeMode) -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = stringResource(R.string.settings_title), style = MaterialTheme.typography.titleMedium)
            ThemeSelector(settingsState.themeMode, onThemeSelected)
        }
    }
}

@Composable
private fun ThemeSelector(selected: ThemeMode, onThemeSelected: (ThemeMode) -> Unit) {
    Text(text = stringResource(R.string.settings_theme))
    Row(
        modifier = Modifier.padding(top = 8.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        androidx.compose.material3.Icon(Icons.Default.WbSunny, contentDescription = null)
        Switch(
            checked = selected == ThemeMode.DARK,
            onCheckedChange = { isDark ->
                onThemeSelected(if (isDark) ThemeMode.DARK else ThemeMode.LIGHT)
            }
        )
        androidx.compose.material3.Icon(Icons.Default.DarkMode, contentDescription = null)
        Text(text = stringResource(R.string.settings_theme_toggle_label))
    }
}
