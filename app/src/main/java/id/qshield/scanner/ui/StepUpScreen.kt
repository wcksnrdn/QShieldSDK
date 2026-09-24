package id.qshield.scanner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.qshield.scanner.data.models.QrisVerificationResponse

/**
 * Layar step-up (Compact-3 + Compact-4 dalam mockup).
 *
 * Ditampilkan saat action=step_up. Kartu berwarna putih dengan border
 * orange, berisi info merchant, alasan mismatch, dan skor risiko.
 * Tombol "Proceed" membuka dialog konfirmasi "Are you sure?" (Compact-4).
 */
@Composable
fun StepUpScreen(
    response: QrisVerificationResponse,
    rawPayload: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onManualInput: () -> Unit
) {
    val borderColor = Color(0xFFF0742C)   // orange — sesuai step_up
    var showConfirmDialog by remember { mutableStateOf(false) }

    // Compact-4: konfirmasi dialog
    if (showConfirmDialog) {
        StepUpConfirmDialog(
            onYes = {
                showConfirmDialog = false
                onConfirm()
            },
            onCancel = {
                showConfirmDialog = false
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top bar ──────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "QShield",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Row {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.FlashOn, "Flash", tint = Color.White)
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Settings, "Settings", tint = Color.White)
                    }
                }
            }

            // ── Step-Up Card (Compact-3) ─────────────────────────
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .border(3.dp, borderColor, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            // Title
                            Text(
                                text = "Are you sure?",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Black
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Merchant Info
                            response.merchant?.let { merchant ->
                                merchant.name?.let {
                                    Text(
                                        text = "Name: $it",
                                        fontSize = 14.sp,
                                        color = Color.Black
                                    )
                                }
                                merchant.city?.let {
                                    Text(
                                        text = "City: $it",
                                        fontSize = 14.sp,
                                        color = Color.Black
                                    )
                                }
                                merchant.nmid?.let {
                                    Text(
                                        text = "NMID: $it",
                                        fontSize = 14.sp,
                                        color = Color.Black
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Reasons — mismatch details from backend
                            if (response.reasons.isNotEmpty()) {
                                Text(
                                    text = "There's mismatch in location and:",
                                    fontSize = 13.sp,
                                    color = Color.Black.copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                response.reasons.forEach { reason ->
                                    Text(
                                        text = "\"$reason\"",
                                        fontSize = 13.sp,
                                        color = Color.Black.copy(alpha = 0.8f),
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Risk score in red
                            Text(
                                text = "Score: ${response.risk_score}/100",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Red,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Proceed button (opens confirm dialog)
                            OutlinedButton(
                                onClick = { showConfirmDialog = true },
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color.Black
                                )
                            ) {
                                Text("Proceed", fontWeight = FontWeight.Bold)
                            }
                        }

                        // Close (X) button top-right
                        IconButton(
                            onClick = onCancel,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .size(28.dp)
                                .clip(CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // ── Bottom bar ───────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF1A1A1A),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "You can also",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onManualInput,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text("Manually input code", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}


/**
 * Dialog konfirmasi step-up (Compact-4 dalam mockup).
 *
 * Kartu kecil dengan border orange, teks "Are you sure?", dan dua tombol:
 * Cancel (hitam) dan Yes (putih/outline).
 */
@Composable
private fun StepUpConfirmDialog(
    onYes: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        containerColor = Color.White,
        shape = RoundedCornerShape(12.dp),
        title = null,
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, Color(0xFFF0742C), RoundedCornerShape(10.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Are you sure?",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Cancel button (dark)
                        Button(
                            onClick = onCancel,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Black,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        // Yes button (light)
                        OutlinedButton(
                            onClick = onYes,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.Black
                            )
                        ) {
                            Text("Yes", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}
