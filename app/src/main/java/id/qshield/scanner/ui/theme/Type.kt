package id.qshield.scanner.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Skala huruf Q-Shield.
 *
 * Sebelumnya cuma `bodyLarge` yang diisi, sisanya memakai bawaan
 * Material — dan bawaannya bernada netral-dokumen: judul tipis, jarak
 * huruf longgar. Di aplikasi pembayaran, angka dan putusan harus
 * membaca TEGAS, karena orang membacanya sambil berdiri di depan kasir.
 *
 * Yang diubah dan alasannya:
 *
 *   display/headline  bobot naik ke Bold, letterSpacing dirapatkan.
 *                     Ini yang dipakai layar putusan — "AMAN" atau
 *                     "BERHENTI" harus terbaca dalam sekali lihat.
 *   title             SemiBold, supaya judul kartu terpisah jelas dari
 *                     isinya tanpa perlu garis pemisah.
 *   body              letterSpacing dikecilkan dari 0,5 ke 0,15.
 *                     Teks alasan kita panjang-panjang; jarak longgar
 *                     membuatnya terbaca lebih lambat.
 *   label             Medium, untuk tombol dan chip.
 */
private val Keluarga = FontFamily.Default

val Typography = Typography(
    displaySmall = TextStyle(
        fontFamily = Keluarga, fontWeight = FontWeight.Bold,
        fontSize = 34.sp, lineHeight = 40.sp, letterSpacing = (-0.5).sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = Keluarga, fontWeight = FontWeight.Bold,
        fontSize = 30.sp, lineHeight = 36.sp, letterSpacing = (-0.4).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = Keluarga, fontWeight = FontWeight.Bold,
        fontSize = 25.sp, lineHeight = 32.sp, letterSpacing = (-0.3).sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = Keluarga, fontWeight = FontWeight.SemiBold,
        fontSize = 21.sp, lineHeight = 28.sp, letterSpacing = (-0.2).sp,
    ),
    titleLarge = TextStyle(
        fontFamily = Keluarga, fontWeight = FontWeight.SemiBold,
        fontSize = 19.sp, lineHeight = 26.sp, letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = Keluarga, fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp, lineHeight = 22.sp, letterSpacing = 0.1.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = Keluarga, fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = Keluarga, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = Keluarga, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 21.sp, letterSpacing = 0.15.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = Keluarga, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 18.sp, letterSpacing = 0.2.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = Keluarga, fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = Keluarga, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = Keluarga, fontWeight = FontWeight.Medium,
        fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp,
    ),
)
