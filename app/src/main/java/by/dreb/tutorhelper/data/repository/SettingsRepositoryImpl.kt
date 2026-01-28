package by.dreb.tutorhelper.data.repository

import by.dreb.tutorhelper.data.preferences.SettingsDataStore
import by.dreb.tutorhelper.domain.model.AppLanguage
import by.dreb.tutorhelper.domain.model.ThemeMode
import by.dreb.tutorhelper.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : SettingsRepository {
    override val themeMode: Flow<ThemeMode> = settingsDataStore.themeMode
    override val language: Flow<AppLanguage> = settingsDataStore.language

    override suspend fun updateTheme(mode: ThemeMode) {
        settingsDataStore.updateTheme(mode)
    }

    override suspend fun updateLanguage(language: AppLanguage) {
        settingsDataStore.updateLanguage(language)
    }
}
