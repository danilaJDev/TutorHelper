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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
            androidx.compose.foundation.layout.Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
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
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = AppPalette.Surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.privacy_policy_for_whom_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(text = stringResource(R.string.privacy_policy_for_whom_text))
                }
            }

            PolicySection(
                title = stringResource(R.string.privacy_policy_data_title),
                body = stringResource(R.string.privacy_policy_data_text)
            )
            PolicySection(
                title = stringResource(R.string.privacy_policy_storage_title),
                body = stringResource(R.string.privacy_policy_storage_text)
            )
            PolicySection(
                title = stringResource(R.string.privacy_policy_sharing_title),
                body = stringResource(R.string.privacy_policy_sharing_text)
            )
            PolicySection(
                title = stringResource(R.string.privacy_policy_access_title),
                body = stringResource(R.string.privacy_policy_access_text)
            )
        }
    }
}

@Composable
private fun PolicySection(title: String, body: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppPalette.Surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(text = body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
