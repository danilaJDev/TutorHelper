package by.dreb.tutorhelper.data.monetization

import android.content.Context
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.monetizationDataStore by preferencesDataStore(name = "monetization_prefs")

@Singleton
class TrialPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val firstLaunchKey = longPreferencesKey("trial_first_launch_epoch_millis")

    val firstLaunchTimeMillis: Flow<Long?> = context.monetizationDataStore.data
        .map { preferences -> preferences[firstLaunchKey] }

    suspend fun setFirstLaunchTimeIfAbsent(timestampMillis: Long) {
        context.monetizationDataStore.edit { preferences ->
            if (preferences[firstLaunchKey] == null) {
                preferences[firstLaunchKey] = timestampMillis
            }
        }
    }
}
