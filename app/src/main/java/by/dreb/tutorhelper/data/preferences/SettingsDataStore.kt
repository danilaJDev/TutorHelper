package by.dreb.tutorhelper.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import by.dreb.tutorhelper.domain.model.AppLanguage
import by.dreb.tutorhelper.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {
    private object Keys {
        val themeMode = stringPreferencesKey("theme_mode")
        val language = stringPreferencesKey("language")
    }

    val themeMode: Flow<ThemeMode> = context.settingsDataStore.data.map { prefs ->
        prefs[Keys.themeMode]?.let { ThemeMode.valueOf(it) } ?: ThemeMode.SYSTEM
    }

    val language: Flow<AppLanguage> = context.settingsDataStore.data.map { prefs ->
        prefs[Keys.language]?.let { AppLanguage.valueOf(it) } ?: AppLanguage.SYSTEM
    }

    suspend fun updateTheme(mode: ThemeMode) {
        context.settingsDataStore.edit { it[Keys.themeMode] = mode.name }
    }

    suspend fun updateLanguage(language: AppLanguage) {
        context.settingsDataStore.edit { it[Keys.language] = language.name }
    }
}
