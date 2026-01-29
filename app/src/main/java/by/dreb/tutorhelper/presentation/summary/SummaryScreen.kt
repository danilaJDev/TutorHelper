package by.dreb.tutorhelper.presentation.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import by.dreb.tutorhelper.R
import by.dreb.tutorhelper.domain.model.AppLanguage
import by.dreb.tutorhelper.domain.model.ThemeMode
import by.dreb.tutorhelper.presentation.settings.SettingsUiState

@Composable
fun SummaryScreen(
    settingsState: SettingsUiState,
    onThemeSelected: (ThemeMode) -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    viewModel: SummaryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.summary_title),
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(text = stringResource(R.string.summary_subtitle))
            }
        }
        item { SummaryHeroCard(state.summary) }
        item { SummaryStatsGrid(state.summary) }
        item { InsightsCard(state.summary) }
        item {
            SettingsCard(
                settingsState = settingsState,
                onThemeSelected = onThemeSelected,
                onLanguageSelected = onLanguageSelected
            )
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
    onThemeSelected: (ThemeMode) -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = stringResource(R.string.settings_title), style = MaterialTheme.typography.titleMedium)
            ThemeSelector(settingsState.themeMode, onThemeSelected)
            LanguageSelector(settingsState.language, onLanguageSelected)
        }
    }
}

@Composable
private fun ThemeSelector(selected: ThemeMode, onThemeSelected: (ThemeMode) -> Unit) {
    Text(text = stringResource(R.string.settings_theme))
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ThemeMode.entries.forEach { mode ->
            androidx.compose.foundation.layout.Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                androidx.compose.material3.RadioButton(
                    selected = selected == mode,
                    onClick = { onThemeSelected(mode) }
                )
                Text(
                    text = when (mode) {
                        ThemeMode.SYSTEM -> stringResource(R.string.settings_theme_system)
                        ThemeMode.LIGHT -> stringResource(R.string.settings_theme_light)
                        ThemeMode.DARK -> stringResource(R.string.settings_theme_dark)
                    }
                )
            }
        }
    }
}

@Composable
private fun LanguageSelector(selected: AppLanguage, onLanguageSelected: (AppLanguage) -> Unit) {
    Text(text = stringResource(R.string.settings_language))
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AppLanguage.entries.forEach { language ->
            androidx.compose.foundation.layout.Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                androidx.compose.material3.RadioButton(
                    selected = selected == language,
                    onClick = { onLanguageSelected(language) }
                )
                Text(
                    text = when (language) {
                        AppLanguage.SYSTEM -> stringResource(R.string.settings_language_system)
                        AppLanguage.EN -> stringResource(R.string.settings_language_en)
                        AppLanguage.RU -> stringResource(R.string.settings_language_ru)
                    }
                )
            }
        }
    }
}
