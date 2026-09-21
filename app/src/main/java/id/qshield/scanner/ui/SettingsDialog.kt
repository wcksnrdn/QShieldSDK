package id.qshield.scanner.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import id.qshield.scanner.data.local.PreferencesDataStore
import kotlinx.coroutines.launch

@Composable
fun SettingsDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val dataStore = remember { PreferencesDataStore(context) }
    val scope = rememberCoroutineScope()
    
    val baseUrl by dataStore.baseUrlFlow.collectAsState(initial = "")
    val apiKey by dataStore.apiKeyFlow.collectAsState(initial = "")
    
    var editBaseUrl by remember(baseUrl) { mutableStateOf(baseUrl) }
    var editApiKey by remember(apiKey) { mutableStateOf(apiKey) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pengaturan") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = editBaseUrl,
                    onValueChange = { editBaseUrl = it },
                    label = { Text("Base URL") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = editApiKey,
                    onValueChange = { editApiKey = it },
                    label = { Text("API Key") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch {
                        dataStore.saveBaseUrl(editBaseUrl)
                        dataStore.saveApiKey(editApiKey)
                        onDismiss()
                    }
                }
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
