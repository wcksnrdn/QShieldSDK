package id.qshield.scanner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import kotlinx.coroutines.delay

/**
 * Layar cooling-off (Compact-5 dalam mockup).
 *
 * Kartu merah solid dengan peringatan keras. Pengguna TIDAK bisa
 * melanjutkan — tidak ada tombol Proceed. Countdown timer menunjukkan
 * sisa detik sebelum bisa kembali.
 */
@Composable
fun CoolingOffScreen(
    response: QrisVerificationResponse,
    remainingSeconds: Int,
    onDismiss: () -> Unit
) {
    var secondsLeft by remember { mutableIntStateOf(remainingSeconds) }

    // Countdown timer
    LaunchedEffect(remainingSeconds) {
        secondsLeft = remainingSeconds
        while (secondsLeft > 0) {
            delay(1000L)
            secondsLeft--
        }
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

            // ── Spacer to push card to upper-center area ─────────
            Spacer(modifier = Modifier.weight(0.3f))

            // ── Red Warning Card (Compact-5) ─────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE5484D) // merah solid
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box {
                        Column(
                            modifier = Modifier
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // "WARNING" label
                            Text(
                                text = "WARNING",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier
                                    .fillMaxWidth(),
                                textAlign = TextAlign.Start
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Warning message
                            Text(
                                text = "Do not proceed this payment, it's highly potential fraud.",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                lineHeight = 22.sp
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Risk score
                            Text(
                                text = "Score: ${response.risk_score}/100",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )

                            // Countdown
                            if (secondsLeft > 0) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Wait ${secondsLeft}s",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
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
                                .background(Color.White.copy(alpha = 0.3f))
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

            Spacer(modifier = Modifier.weight(0.7f))
        }
    }
}
