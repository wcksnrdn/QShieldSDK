package id.qshield.scanner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.qshield.scanner.data.models.QrisVerificationResponse
import id.qshield.scanner.data.models.VerdictAction
import id.qshield.scanner.ui.theme.ActionColorMapper

/**
 * Layar putusan (Compact-2 dalam mockup).
 *
 * Menampilkan kartu dengan info merchant, alasan dari backend, dan skor
 * risiko. Warna kartu mengikuti aksi: kuning untuk warn, hijau untuk
 * proceed. Tombol "Proceed" hanya ditampilkan jika aksi bukan cooling_off.
 */
@Composable
fun VerdictScreen(
    response: QrisVerificationResponse,
    rawPayload: String,
    onProceed: () -> Unit,
    onDismiss: () -> Unit,
    onManualInput: () -> Unit
) {
    val actionColor = ActionColorMapper.mapActionToColor(response.action)
    val verdictAction = response.verdictAction

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

            // ── Verdict Card ─────────────────────────────────────
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = actionColor),
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

                            // Reasons from backend
                            if (response.reasons.isNotEmpty()) {
                                response.reasons.forEach { reason ->
                                    Text(
                                        text = "\"$reason\"",
                                        fontSize = 13.sp,
                                        color = Color.Black.copy(alpha = 0.8f),
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Risk score
                            Text(
                                text = "Score: ${response.risk_score}/100",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Red,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Proceed button
                            if (verdictAction != VerdictAction.COOLING_OFF) {
                                OutlinedButton(
                                    onClick = onProceed,
                                    modifier = Modifier.align(Alignment.CenterHorizontally),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color.Black
                                    )
                                ) {
                                    Text("Proceed", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Close (X) button top-right
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .size(28.dp)
                                .clip(CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // ── Bottom: "You can also" ───────────────────────────
            VerdictBottomBar(onManualInput = onManualInput)
        }
    }
}


@Composable
private fun VerdictBottomBar(onManualInput: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.Start
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
