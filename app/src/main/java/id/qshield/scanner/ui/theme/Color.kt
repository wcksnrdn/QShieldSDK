package id.qshield.scanner.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Palet Q-Shield.
 *
 * Sebelumnya berkas ini masih memakai Purple40/Purple80 bawaan template
 * Android Studio, dan `dynamicColor` menyala — sehingga warna aplikasi
 * ikut wallpaper tiap HP. Aplikasi yang sama tampil berbeda di tiap
 * perangkat demo, dan warna aksi bisa bertabrakan dengan primary yang
 * tidak pernah kita pilih.
 *
 * Nilai di sini TIDAK dikarang dari nol. Layar-layar verifikasi sudah
 * dirancang dengan permukaan gelap 1A1A1A dan aksen teal 007A8C, jadi
 * tema mengikuti yang sudah ada — kalau tidak, komponen bertema dan
 * komponen berwarna-keras akan tampil sebagai dua aplikasi berbeda di
 * layar yang sama.
 *
 * Warna aksi (hijau 22C08A, amber F0A82C, oranye F0742C, merah E5484D)
 * dibiarkan apa adanya di ActionColorMapper: itu bahasa putusan, dan
 * sengaja sama dengan scanner web supaya demo di dua perangkat tidak
 * memberi kesan dua sistem berbeda.
 */

// --- Identitas utama ------------------------------------------------
//
// 007A8C diambil persis dari tombol yang sudah ada. Teal kebiruan ini
// sengaja BUKAN biru dompet digital besar — aplikasi yang menyerupainya
// terbaca sebagai tiruan, bukan sebagai produk sendiri.
val TealUtama = Color(0xFF007A8C)
val TealTerang = Color(0xFF3FC9DC)     // untuk teks/ikon di atas gelap
val TealKontainer = Color(0xFF00505C)
val OnTealKontainer = Color(0xFFB8ECF4)

// --- Permukaan gelap ------------------------------------------------
val LatarGelap = Color(0xFF121514)
val PermukaanGelap = Color(0xFF1A1A1A)   // sama dengan kartu yang sudah ada
val PermukaanNaik = Color(0xFF242625)    // kartu di atas kartu
val OnPermukaanGelap = Color(0xFFECEFEE)
val OnPermukaanVarianGelap = Color(0xFFA8AFAD)
val GarisGelap = Color(0xFF343938)

// --- Permukaan terang, dipakai kalau tema terang dinyalakan ---------
val LatarTerang = Color(0xFFF5F8F8)
val PermukaanTerang = Color(0xFFFFFFFF)
val PermukaanVarianTerang = Color(0xFFE6EDEE)
val OnPermukaanTerang = Color(0xFF14201F)
val OnPermukaanVarianTerang = Color(0xFF48585A)
val GarisTerang = Color(0xFFD3DEDF)

// --- Bahaya ---------------------------------------------------------
//
// Disamakan dengan merah cooling_off supaya pesan galat dan putusan
// "berhenti" tidak pernah tampil sebagai dua merah berbeda.
val MerahBahaya = Color(0xFFE5484D)
val MerahKontainerGelap = Color(0xFF5E1C1F)
val OnMerahKontainerGelap = Color(0xFFFFDAD8)
val MerahKontainerTerang = Color(0xFFFFDAD8)
val OnMerahKontainerTerang = Color(0xFF410004)
