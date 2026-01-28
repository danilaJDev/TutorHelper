package by.dreb.tutorhelper.presentation.settings

import by.dreb.tutorhelper.domain.model.AppLanguage
import by.dreb.tutorhelper.domain.model.ThemeMode

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: AppLanguage = AppLanguage.SYSTEM
) {
    val languageTag: String? = language.tag
}
