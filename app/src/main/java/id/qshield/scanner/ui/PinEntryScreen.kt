package id.qshield.scanner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
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

/**
 * Layar PIN entry (Compact-6 dalam mockup).
 *
 * Tampil setelah step-up dikonfirmasi. Pengguna memasukkan PIN 6 digit.
 * Dot indicators menunjukkan digit yang sudah diisi. Layout mengikuti
 * mockup: header QShield, 6 dots, 3×3+1 numpad.
 */
@Composable
fun PinEntryScreen(
    onPinComplete: (String) -> Unit,
    onCancel: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    val pinLength = 6

    // Auto-submit saat lengkap
    LaunchedEffect(pin) {
        if (pin.length == pinLength) {
            onPinComplete(pin)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Header ───────────────────────────────────────────
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "QShield",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(40.dp))

            // ── PIN Dots ─────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pinLength) { index ->
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(
                                if (index < pin.length) Color.Black
                                else Color(0xFFD0D0D0)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // ── Number Pad ───────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Row 1: 1, 2, 3
                NumberRow(
                    numbers = listOf("1", "2", "3"),
                    onDigit = { if (pin.length < pinLength) pin += it }
                )
                // Row 2: 4, 5, 6
                NumberRow(
                    numbers = listOf("4", "5", "6"),
                    onDigit = { if (pin.length < pinLength) pin += it }
                )
                // Row 3: 7, 8, 9
                NumberRow(
                    numbers = listOf("7", "8", "9"),
                    onDigit = { if (pin.length < pinLength) pin += it }
                )
                // Row 4: (empty), 0, backspace
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Empty spacer
                    Box(modifier = Modifier.size(72.dp))

                    // 0
                    NumberKey(
                        text = "0",
                        onClick = { if (pin.length < pinLength) pin += "0" }
                    )

                    // Backspace
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .clickable {
                                if (pin.isNotEmpty()) pin = pin.dropLast(1)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                            contentDescription = "Delete",
                            tint = Color.Black,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Cancel link
            TextButton(onClick = onCancel) {
                Text(
                    text = "Cancel",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun NumberRow(
    numbers: List<String>,
    onDigit: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        numbers.forEach { num ->
            NumberKey(text = num, onClick = { onDigit(num) })
        }
    }
}

@Composable
private fun NumberKey(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(Color(0xFF1A1A1A))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}
