package by.dreb.tutorhelper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.appcompat.app.AppCompatDelegate
import by.dreb.tutorhelper.data.seed.SampleDataSeeder
import by.dreb.tutorhelper.presentation.AppRoot
import by.dreb.tutorhelper.presentation.settings.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var sampleDataSeeder: SampleDataSeeder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        sampleDataSeeder.seedIfEmpty()
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val settingsState by settingsViewModel.state.collectAsState()

            LaunchedEffect(settingsState.languageTag) {
                val locales = settingsState.languageTag?.let { LocaleListCompat.forLanguageTags(it) }
                    ?: LocaleListCompat.getEmptyLocaleList()
                AppCompatDelegate.setApplicationLocales(locales)
            }

            AppRoot(
                settingsState = settingsState,
                onThemeSelected = settingsViewModel::updateTheme
            )
        }
    }
}
