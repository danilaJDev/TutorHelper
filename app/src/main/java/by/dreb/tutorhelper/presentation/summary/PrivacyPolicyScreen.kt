package by.dreb.tutorhelper.presentation.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import by.dreb.tutorhelper.R
import by.dreb.tutorhelper.ui.theme.AppPalette

@Composable
fun PrivacyPolicyScreen(onBackClick: () -> Unit) {
    Scaffold(
        containerColor = AppPalette.Background,
        topBar = {
            Column {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                    Text(
                        text = stringResource(R.string.privacy_policy_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Divider()
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Hero card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = AppPalette.Surface),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.privacy_policy_for_whom_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(R.string.privacy_policy_for_whom_text),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            PolicySection(
                icon = Icons.Default.Storage,
                title = stringResource(R.string.privacy_policy_data_title),
                body = stringResource(R.string.privacy_policy_data_text)
            )

            PolicySection(
                icon = Icons.Default.Lock,
                title = stringResource(R.string.privacy_policy_storage_title),
                body = stringResource(R.string.privacy_policy_storage_text)
            )

            PolicySection(
                icon = Icons.Default.Share,
                title = stringResource(R.string.privacy_policy_sharing_title),
                body = stringResource(R.string.privacy_policy_sharing_text)
            )

            PolicySection(
                icon = Icons.Default.Visibility,
                title = stringResource(R.string.privacy_policy_access_title),
                body = stringResource(R.string.privacy_policy_access_text)
            )
        }
    }
}


@Composable
private fun PolicySection(
    icon: ImageVector,
    title: String,
    body: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = AppPalette.Surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            androidx.compose.foundation.layout.Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

