package id.qshield.scanner.ui

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import id.qshield.scanner.ui.camera.CameraPreviewScreen
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScannerScreen(
    viewModel: ScannerViewModel
) {
    var showSettings by remember { mutableStateOf(false) }
    
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    if (!cameraPermissionState.status.isGranted) {
        PermissionRationaleScreen(
            text = "Kamera dibutuhkan untuk memindai kode QR.",
            onRequestPermission = { cameraPermissionState.launchPermissionRequest() }
        )
        return
    }

    if (!locationPermissionState.status.isGranted) {
        PermissionRationaleScreen(
            text = "Lokasi dibutuhkan untuk verifikasi keamanan QRIS.",
            onRequestPermission = { locationPermissionState.launchPermissionRequest() }
        )
        return
    }

    val uiState by viewModel.uiState.collectAsState()
    val modeKatalog by viewModel.modeKatalog.collectAsState()

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("QShield Scanner") },
                actions = {
                    // Sakelar mode katalog ditaruh di sini, bukan di dalam
                    // dialog setelan: ia dinyalakan dan dimatikan berkali-kali
                    // dalam satu sesi lapangan, dan pemakainya harus selalu
                    // bisa MELIHAT sedang di mode mana sebelum memindai.
                    Text(
                        text = if (modeKatalog) "Katalog" else "Verifikasi",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Switch(
                        checked = modeKatalog,
                        onCheckedChange = { viewModel.setModeKatalog(it) }
                    )
                    IconButton(onClick = { showSettings = true }) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (showSettings) {
            SettingsDialog(onDismiss = { showSettings = false })
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is ScannerUiState.Scanning -> {
                    CameraPreviewScreen(
                        onQrCodeScanned = { payload ->
                            viewModel.onQrCodeScanned(payload)
                        }
                    )
                }
                is ScannerUiState.Result -> {
                    ResultScreen(
                        result = state.response,
                        wifiNotice = state.wifiNotice,
                        currentPayload = state.currentPayload,
                        onVerifyPrintedLabel = { payload, nmid ->
                            viewModel.onVerifyPrintedLabel(payload, nmid)
                        },
                        onRestart = { viewModel.restartScanning() }
                    )
                }
                is ScannerUiState.Katalog -> {
                    KatalogScreen(
                        hasil = state.response,
                        onRestart = { viewModel.restartScanning() }
                    )
                }
                is ScannerUiState.Error -> {
                    ErrorScreen(
                        message = state.message,
                        onRetry = { viewModel.restartScanning() }
                    )
                }
            }
        }
    }
}

@Composable
fun PermissionRationaleScreen(
    text: String,
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRequestPermission) {
            Text("Berikan Izin")
        }
    }
}
