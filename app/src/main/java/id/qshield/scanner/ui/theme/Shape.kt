package id.qshield.scanner.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Sudut membulat, lebih besar daripada bawaan Material 3.
 *
 * Bawaannya 4/8/12/16/28 dp. Di aplikasi pembayaran, kartu dan tombol
 * yang sudutnya tajam terbaca seperti formulir administratif; sudut
 * yang lebih bulat membuat tiap blok terbaca sebagai satu objek yang
 * bisa disentuh.
 *
 * `extraLarge` sengaja 28 dp dan bukan lebih: di atas itu, kartu lebar
 * mulai terlihat seperti pil, dan teks di dalamnya kehilangan sudut
 * yang bisa dipakai mata sebagai jangkar baca.
 */
val Bentuk = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
)
