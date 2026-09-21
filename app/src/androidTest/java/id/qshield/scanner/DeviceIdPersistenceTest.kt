package id.qshield.scanner

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import id.qshield.scanner.data.local.PreferencesDataStore
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pengenal perangkat harus bertahan antar peluncuran aplikasi.
 *
 * Versi sebelumnya membangkitkan UUID di dalam ScannerViewModel, di memori,
 * sehingga setiap kali aplikasi dibuka server melihat perangkat baru.
 *
 * Itu bukan sekadar soal kerapian. MIN_OBSERVERS = 3 di sisi server adalah
 * inti pertahanan Q-Shield: sebuah stiker baru dianggap sah hanya setelah
 * tiga PERANGKAT BERBEDA mengamatinya. Dengan pengenal yang lahir ulang
 * tiap peluncuran, satu orang dengan satu HP memenuhi syarat itu cukup
 * dengan menutup dan membuka aplikasi tiga kali — konsensus yang
 * seharusnya mahal menjadi gratis, dan penipu bisa mengesahkan stikernya
 * sendiri.
 *
 * Test ini instrumented, bukan unit, karena DataStore menulis ke berkas
 * sungguhan. Mengujinya dengan tiruan hanya akan menguji tiruannya.
 */
@RunWith(AndroidJUnit4::class)
class DeviceIdPersistenceTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun pengenal_sama_saat_dipanggil_berulang() = runBlocking {
        val prefs = PreferencesDataStore(context)
        val pertama = prefs.getOrGenerateUuid()
        val kedua = prefs.getOrGenerateUuid()
        assertEquals("pengenal berubah di antara dua panggilan", pertama, kedua)
    }

    @Test
    fun pengenal_bertahan_saat_objeknya_dibuat_ulang() = runBlocking {
        // Ini bentuk bug yang sebenarnya: ScannerViewModel dan seluruh
        // graf objeknya dibangun ulang tiap aplikasi dibuka. Yang diuji
        // di sini bukan satu instance yang mengingat, melainkan nilainya
        // yang bertahan di luar instance mana pun.
        val sebelum = PreferencesDataStore(context).getOrGenerateUuid()
        val sesudah = PreferencesDataStore(context).getOrGenerateUuid()
        assertEquals(
            "pengenal lahir ulang saat objek dibuat ulang — " +
                "konsensus MIN_OBSERVERS bisa dipalsukan satu orang",
            sebelum, sesudah,
        )
    }

    @Test
    fun pengenal_memenuhi_kontrak_server() = runBlocking {
        // Server menolak dengan 422 di luar pola ini. Pernah terjadi
        // berulang kali pada skrip probe: pengenal tujuh karakter membuat
        // seluruh permintaan gagal, dan gagalnya tidak kelihatan.
        val id = PreferencesDataStore(context).getOrGenerateUuid()
        assertTrue(
            "pengenal '$id' ditolak server: harus ^[A-Za-z0-9_-]{8,64}$",
            Regex("^[A-Za-z0-9_-]{8,64}$").matches(id),
        )
    }
}
