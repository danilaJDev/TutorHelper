package by.dreb.tutorhelper.presentation.students

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.LocalPhone
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import by.dreb.tutorhelper.R
import by.dreb.tutorhelper.presentation.components.FormCardSection

@Composable
fun StudentCreateScreen(
    onBackClick: () -> Unit,
    viewModel: StudentCreateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var defaultPrice by remember { mutableStateOf("") }
    var telegram by remember { mutableStateOf("") }
    var useViber by remember { mutableStateOf(false) }
    var useWhatsApp by remember { mutableStateOf(false) }

    val leadingIconColors = OutlinedTextFieldDefaults.colors(
        focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
        unfocusedLeadingIconColor = MaterialTheme.colorScheme.primary,
        disabledLeadingIconColor = MaterialTheme.colorScheme.primary,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline
    )
    val isValid = name.isNotBlank()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.consumeError()
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onBackClick()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
            }
            Text(
                text = stringResource(R.string.student_create_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            )
            TextButton(onClick = {
                viewModel.createStudent(
                    name = name,
                    phone = phone.ifBlank { null },
                    telegramUsername = telegram.trim().ifBlank { null },
                    viberPhone = if (useViber) phone.ifBlank { null } else null,
                    whatsappPhone = if (useWhatsApp) phone.ifBlank { null } else null,
                    note = note.ifBlank { null },
                    defaultPrice = defaultPrice.toDoubleOrNull() ?: 0.0
                )
            }, enabled = isValid && !uiState.isSaving) {
                Text(text = "Сохранить", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FormCardSection(title = "Данные ученика") {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(stringResource(R.string.student_label_name)) }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.Person, null) }, colors = leadingIconColors, shape = RoundedCornerShape(12.dp), singleLine = true)
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text(stringResource(R.string.student_label_phone)) }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.LocalPhone, null) }, colors = leadingIconColors, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), shape = RoundedCornerShape(12.dp), singleLine = true)
            }

            FormCardSection(title = "Мессенджеры") {
                OutlinedTextField(
                    value = telegram,
                    onValueChange = { telegram = it },
                    label = { Text("Telegram (@username)") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Chat, null) },
                    colors = leadingIconColors,
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Viber (номер из телефона)", modifier = Modifier.weight(1f))
                    Switch(checked = useViber, onCheckedChange = { useViber = it })
                }
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("WhatsApp (номер из телефона)", modifier = Modifier.weight(1f))
                    Switch(checked = useWhatsApp, onCheckedChange = { useWhatsApp = it })
                }
            }

            FormCardSection(title = "Оплата и заметки") {
                OutlinedTextField(value = defaultPrice, onValueChange = { defaultPrice = it }, label = { Text(stringResource(R.string.student_label_default_price)) }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.Payments, null) }, colors = leadingIconColors, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), shape = RoundedCornerShape(12.dp), singleLine = true)
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text(stringResource(R.string.student_label_note)) }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.AutoMirrored.Filled.StickyNote2, null) }, colors = leadingIconColors, shape = RoundedCornerShape(12.dp), singleLine = true)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
