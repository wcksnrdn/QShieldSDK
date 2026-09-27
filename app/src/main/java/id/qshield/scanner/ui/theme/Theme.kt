package id.qshield.scanner.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val SkemaGelap = darkColorScheme(
    primary = TealUtama,
    onPrimary = PermukaanTerang,
    primaryContainer = TealKontainer,
    onPrimaryContainer = OnTealKontainer,

    secondary = TealTerang,
    onSecondary = LatarGelap,
    secondaryContainer = TealKontainer,
    onSecondaryContainer = OnTealKontainer,

    tertiary = TealTerang,
    onTertiary = LatarGelap,

    background = LatarGelap,
    onBackground = OnPermukaanGelap,
    surface = PermukaanGelap,
    onSurface = OnPermukaanGelap,
    surfaceVariant = PermukaanNaik,
    onSurfaceVariant = OnPermukaanVarianGelap,
    outline = GarisGelap,
    outlineVariant = GarisGelap,

    error = MerahBahaya,
    onError = PermukaanTerang,
    errorContainer = MerahKontainerGelap,
    onErrorContainer = OnMerahKontainerGelap,
)

private val SkemaTerang = lightColorScheme(
    primary = TealUtama,
    onPrimary = PermukaanTerang,
    primaryContainer = PermukaanVarianTerang,
    onPrimaryContainer = OnPermukaanTerang,

    secondary = TealUtama,
    onSecondary = PermukaanTerang,

    background = LatarTerang,
    onBackground = OnPermukaanTerang,
    surface = PermukaanTerang,
    onSurface = OnPermukaanTerang,
    surfaceVariant = PermukaanVarianTerang,
    onSurfaceVariant = OnPermukaanVarianTerang,
    outline = GarisTerang,
    outlineVariant = GarisTerang,

    error = MerahBahaya,
    onError = PermukaanTerang,
    errorContainer = MerahKontainerTerang,
    onErrorContainer = OnMerahKontainerTerang,
)

/**
 * Tema aplikasi.
 *
 * `gelap` sengaja dipatok true, bukan mengikuti setelan sistem.
 *
 * Alasannya sama dengan mematikan dynamic color: layar-layar verifikasi
 * sudah dirancang di atas permukaan gelap dengan sebagian warna ditulis
 * langsung di kode. Mengikuti setelan sistem berarti separuh tampilan
 * berubah dan separuh lagi tidak — dan itu muncul persis di HP anggota
 * tim yang kebetulan memakai mode terang, biasanya pada hari demo.
 *
 * Kalau nanti seluruh warna keras sudah dipindah ke token tema, baris
 * ini tinggal dikembalikan ke `isSystemInDarkTheme()`.
 */
@Composable
fun QShieldTheme(
    gelap: Boolean = true,
    content: @Composable () -> Unit
) {
    val skema = if (gelap) SkemaGelap else SkemaTerang

    // Bilah status mengikuti latar aplikasi. Tanpa ini ada pita hitam
    // di atas layar yang membuat aplikasi terlihat belum selesai.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = skema.background.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !gelap
        }
    }

    MaterialTheme(
        colorScheme = skema,
        typography = Typography,
        shapes = Bentuk,
        content = content
    )
}
