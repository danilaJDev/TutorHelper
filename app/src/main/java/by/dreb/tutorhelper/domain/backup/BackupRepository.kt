package by.dreb.tutorhelper.domain.backup

import android.net.Uri

interface BackupRepository {
    suspend fun exportToUri(uri: Uri)
    suspend fun importFromUri(uri: Uri)
}
