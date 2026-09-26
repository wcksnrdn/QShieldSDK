package id.qshield.scanner.ui

import android.Manifest
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.*
import id.qshield.scanner.data.models.QShieldFlowState
import id.qshield.scanner.data.models.VerdictAction
import id.qshield.scanner.ui.camera.CameraPreviewScreen

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScannerScreen(
    viewModel: ScannerViewModel,
    onNavigateBack: () -> Unit = {}
) {
    var showSettings by remember { mutableStateOf(false) }
    var showManualInput by remember { mutableStateOf(false) }

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

    val flowState by viewModel.flowState.collectAsState()
    val modeKatalog by viewModel.modeKatalog.collectAsState()

    if (showSettings) {
        SettingsDialog(onDismiss = { showSettings = false })
    }

    if (showManualInput) {
        ManualInputDialog(
            onDismiss = { showManualInput = false },
            onSubmit = { code ->
                showManualInput = false
                viewModel.onQrCodeScanned(code)
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        when (val state = flowState) {
            is QShieldFlowState.Launcher,
            is QShieldFlowState.Scanning -> {
                // Camera + QShield-branded scanner UI (Compact-1)
                CameraPreviewScreen(
                    onQrCodeScanned = { payload ->
                        viewModel.onQrCodeScanned(payload)
                    }
                )

                // Top bar: "QShield" title + action icons
                QShieldTopBar(
                    modeKatalog = modeKatalog,
                    onToggleKatalog = { viewModel.setModeKatalog(!modeKatalog) },
                    onSettingsClick = { showSettings = true }
                )

                // Bottom: "You can also" + manual input
                QShieldBottomBar(
                    onManualInput = { showManualInput = true }
                )
            }

            is QShieldFlowState.Verifying -> {
                // Loading state over dark background
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Memverifikasi...",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            is QShieldFlowState.Verdict -> {
                VerdictScreen(
                    response = state.response,
                    rawPayload = state.rawPayload,
                    onProceed = { viewModel.onVerdictProceed() },
                    onDismiss = { viewModel.restartScanning() },
                    onManualInput = { /* TODO */ }
                )
            }

            is QShieldFlowState.StepUpConfirmation -> {
                StepUpScreen(
                    response = state.response,
                    rawPayload = state.rawPayload,
                    onConfirm = { viewModel.onStepUpConfirmed() },
                    onCancel = { viewModel.restartScanning() },
                    onManualInput = { /* TODO */ }
                )
            }

            is QShieldFlowState.CoolingOff -> {
                CoolingOffScreen(
                    response = state.response,
                    remainingSeconds = state.remainingSeconds,
                    onDismiss = { viewModel.restartScanning() }
                )
            }

            is QShieldFlowState.PinEntry -> {
                PinEntryScreen(
                    onPinComplete = { pin -> viewModel.onPinEntered(pin) },
                    onCancel = { viewModel.restartScanning() }
                )
            }

            is QShieldFlowState.Success -> {
                SuccessScreen(
                    merchantName = state.merchantName,
                    onDone = { viewModel.restartScanning() }
                )
            }

            is QShieldFlowState.ErrorState -> {
                ErrorScreen(
                    title = state.title,
                    message = state.message,
                    canRetry = state.canRetry,
                    onRetry = { viewModel.restartScanning() }
                )
            }
        }
    }
}


// ── QShield Top Bar (Compact-1 header) ───────────────────────────

@Composable
private fun QShieldTopBar(
    modeKatalog: Boolean,
    onToggleKatalog: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo / Title
        Text(
            text = "QShield",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Mode toggle
            FilterChip(
                selected = modeKatalog,
                onClick = onToggleKatalog,
                label = {
                    Text(
                        if (modeKatalog) "Katalog" else "Verifikasi",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.Black.copy(alpha = 0.4f),
                    selectedContainerColor = Color(0xFF007A8C).copy(alpha = 0.7f)
                ),
                border = null
            )

            // Lightning bolt icon
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = "Flash",
                    tint = Color.White
                )
            }

            // Settings gear
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.White
                )
            }
        }
    }
}


// ── QShield Bottom Bar (Compact-1 bottom) ────────────────────────

@Composable
private fun BoxScope.QShieldBottomBar(
    onManualInput: () -> Unit
) {
    Column(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
    ) {
        // Dark card: "You can also"
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF1A1A1A),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp)
                    .padding(top = 32.dp, bottom = 32.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "You can also",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // "Manually input code" button
                OutlinedButton(
                    onClick = onManualInput,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true)
                ) {
                    Text(
                        text = "Manually input code",
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}


// ── Manual Input Dialog (interactive QRIS code entry) ────────────

@Composable
private fun ManualInputDialog(
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var payload by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                text = "Masukkan kode manual",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Tempelkan kode QRIS untuk diverifikasi.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = payload,
                    onValueChange = { payload = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 96.dp),
                    placeholder = {
                        Text(
                            text = "00020101021126660014ID.CO.QRIS.WWW...",
                            fontSize = 12.sp
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF007A8C),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                        cursorColor = Color.White
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(payload.trim()) },
                enabled = payload.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF007A8C),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Verifikasi kode ini", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text("Tutup")
            }
        }
    )
}


// ── Permission Rationale ─────────────────────────────────────────

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