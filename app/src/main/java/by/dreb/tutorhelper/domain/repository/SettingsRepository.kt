package by.dreb.tutorhelper.domain.repository

import by.dreb.tutorhelper.domain.model.AppLanguage
import by.dreb.tutorhelper.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val themeMode: Flow<ThemeMode>
    val language: Flow<AppLanguage>
    suspend fun updateTheme(mode: ThemeMode)
    suspend fun updateLanguage(language: AppLanguage)
}
