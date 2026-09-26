package id.qshield.scanner.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Aksi putusan dari backend. Nilai string-nya COCOK PERSIS dengan
 * apa yang dikembalikan binding.py: "proceed", "warn", "step_up",
 * "cooling_off".
 *
 * UNKNOWN ditambahkan sebagai jaring pengaman di sisi klien — kalau
 * server mengembalikan nilai baru yang belum dikenal SDK, deserialisasi
 * tidak meledak.
 */
@Serializable
enum class VerdictAction {
    @SerialName("proceed") PROCEED,
    @SerialName("warn") WARN,
    @SerialName("step_up") STEP_UP,
    @SerialName("cooling_off") COOLING_OFF,
    UNKNOWN;

    companion object {
        /**
         * Parse dari string mentah — untuk backward compat dengan kode
         * yang masih memegang action sebagai String.
         */
        fun fromString(value: String): VerdictAction {
            return entries.find { it.name.equals(value, ignoreCase = true) }
                ?: entries.find { it.name.equals(value.replace("-", "_"), ignoreCase = true) }
                ?: UNKNOWN
        }
    }
}

// ── Request models ───────────────────────────────────────────────

@Serializable
data class QrisVerificationRequest(
    val payload: String,
    val lat: Double,
    val lng: Double,
    val accuracy_m: Float,
    val device_anon_id: String,
    val device_integrity: DeviceIntegrity? = null,
    val ambient_wifi: AmbientWifi? = null,
    val printed_label: PrintedLabel? = null,

    /**
     * Pemindaian dari GAMBAR, bukan dari stiker di depan mata.
     *
     * Kasusnya sehari-hari: seseorang memfoto QR di warung, mengirimnya
     * lewat pesan, dan orang lain membayar dari tempat yang berbeda.
     * Koordinat pembayar NYATA, tapi tidak mengatakan apa pun tentang
     * letak stikernya.
     *
     * WAJIB dinyalakan pada alur pilih-dari-galeri. Tanpa ini, tiap
     * pembayaran jarak jauh menanam jangkar palsu di lokasi pembayar,
     * dan tiap jangkar berjarak >1 km menambah hukuman PERMANEN pada
     * merchant yang sah.
     *
     * Hanya bisa MENGETATKAN: menyalakannya membuang verifikasi
     * penempatan, tidak pernah menerbitkannya.
     */
    val from_image: Boolean = false,

    /**
     * Sumber koordinat: "live" atau "replay".
     *
     * "replay" dipakai saat koordinatnya berasal dari rekaman, bukan
     * GPS langsung — jalur cadangan demo ketika GPS di dalam gedung
     * tidak bisa dipercaya. Ditandai terbuka di tanggapan dan jejak
     * audit; menyamarkannya sebagai live adalah kebohongan kecil yang
     * akan ketahuan.
     */
    val location_source: String = "live",

    /** Sertakan bedah TLV di tanggapan. Membengkakkan tanggapan ~4x. */
    val include_tlv: Boolean = false
)

@Serializable
data class DeviceIntegrity(
    val mock_location: Boolean,
    val rooted: Boolean,
    /**
     * Hasil Play Integrity / App Attest.
     *
     * BOOLEAN, bukan String — backend mendeklarasikannya `boolean|null`
     * dan membandingkannya dengan `is True` / `is False`. Mengirim
     * string di sini ditolak 422 di batas sistem, dan itu tidak akan
     * terlihat sampai dicoba di perangkat sungguhan.
     *
     * `null` berarti pemeriksaannya TIDAK PERNAH DIJALANKAN — berbeda
     * dari dijalankan lalu gagal, dan backend membedakan keduanya.
     */
    val attested: Boolean? = null,
    val platform: String = "android"
)

@Serializable
data class AmbientWifi(
    val ap_hashes: List<String>
)

@Serializable
data class PrintedLabel(
    val nmid: String? = null,
    val merchant_name: String? = null
)

/**
 * Permintaan ke /api/v1/inspect — TANPA koordinat, dan itu inti bedanya.
 *
 * Dipakai saat mengkatalogkan QRIS, misalnya memotret stiker dari
 * internet untuk memperkaya korpus penerbit. Mengirimkannya ke /verify
 * membuat tempat pemindainya tampak seperti jangkar yang berkali-kali
 * diserang, dan pedagang sungguhan di sekitarnya ikut tertuduh.
 */
@Serializable
data class InspectRequest(
    val payload: String,
    val include_tlv: Boolean = false
)

/**
 * Hasil /inspect. TIDAK ada action, verdict, maupun tiket: tanpa lokasi
 * tidak ada putusan lokasi yang bisa diberikan.
 */
@Serializable
data class InspectResponse(
    val merchant: Merchant? = null,
    /** Apa yang server KETAHUI tentang Merchant ID ini. Selalu terisi di /inspect. */
    val known: AttributionOut? = null,
    val fees: FeeOut? = null,
    val structural_signals: List<String> = emptyList(),
    val structural_reasons: List<String> = emptyList(),
    val processing_ms: Double = 0.0
)

/**
 * Apa yang server KETAHUI tentang sebuah Merchant ID.
 *
 * Bedanya dengan [Merchant] menentukan, dan jangan tertukar:
 *
 *   Merchant          apa yang TERTULIS di payload — bisa diketik
 *                     siapa saja saat mendaftar
 *   AttributionOut    apa yang sudah PERNAH DIAMATI atas Merchant ID itu
 *
 * Nama di QR bisa dikarang; riwayat pengamatan tidak bisa. Pada
 * pembayaran jarak jauh inilah satu-satunya yang masih bisa diberikan:
 * pembayar mencocokkannya sendiri dengan yang ia ketahui.
 *
 * TIDAK memuat koordinat — kota adalah resolusi paling halus yang
 * keluar dari sini.
 */
@Serializable
data class AttributionOut(
    val known: Boolean = false,
    val names: List<String> = emptyList(),
    val city: String? = null,
    val observer_count: Int = 0,
    val locations: Int = 0,
    val first_seen: String? = null,
    val last_seen: String? = null,
    val registered: Boolean = false,
    /** null kalau belum ada nama pembanding yang pernah diamati. */
    val name_matches: Boolean? = null
)

/**
 * Di atas APA putusan ini berdiri.
 *
 * `verdict` menjawab "boleh dibayar atau tidak". Ini menjawab
 * pertanyaan berbeda yang sama pentingnya: seberapa mahal memalsukan
 * dasar putusan itu.
 *
 * [observers] tumbuh dari `device_anon_id` — field yang diisi KLIEN,
 * jadi penyerang bisa menumbuhkannya. [vouched_observers] tidak bisa:
 * ia hanya bertambah ketika atestasi perangkat diperiksa penyelenggara
 * DAN dipertanggungkan lewat kunci API mereka.
 *
 * Kalau [established] true tapi [vouched_observers] nol dan
 * [registered] false, tanggapan juga membawa sinyal
 * `consensus_unvouched`: reputasi tempat itu seluruhnya anonim.
 * Tampilkan apa adanya — ini pengungkapan, bukan skor.
 */
@Serializable
data class EvidenceOut(
    val observers: Int = 0,
    val vouched_observers: Int = 0,
    val registered: Boolean = false,
    val established: Boolean = false,
    val span_hours: Double = 0.0
)

/**
 * Satu entri bedah TLV — panel forensik, hanya di mode demo.
 *
 * Bersarang: template merchant (tag 26-51) punya [children] berisi
 * GUID, PAN, NMID, dan kriteria usahanya.
 */
@Serializable
data class TlvEntry(
    val tag: String = "",
    val label: String = "",
    val length: Int = 0,
    val value: String = "",
    val children: List<TlvEntry> = emptyList()
)

// ── Response models ──────────────────────────────────────────────

/**
 * Biaya di luar nominal yang DIMINTA payload — tag 55, 56, 57.
 *
 * PENGUNGKAPAN, BUKAN SKOR. Ditampilkan apa adanya supaya pengguna bisa
 * melihat bahwa stiker ini meminta biaya tambahan SEBELUM PIN dimasukkan.
 * Dipetakan persis dari backend FeeOut.
 */
@Serializable
data class FeeOut(
    val indicator: String? = null,
    val label: String? = null,
    val fixed: String? = null,
    val percent: String? = null,
    val present: Boolean = false
)

@Serializable
data class Layers(
    val location: Int,
    val behavior: Int
)

@Serializable
data class Merchant(
    val nmid: String? = null,
    val name: String? = null,
    val city: String? = null,
    val criteria: String? = null,
    val is_static: Boolean = false
)

/**
 * Tanggapan /api/v1/verify — dipetakan PERSIS dari backend VerifyResponse.
 *
 * Field baru (fees, verification_ticket, ticket_expires_in) diberi default
 * supaya kode yang sudah jalan tidak rusak.
 */
@Serializable
data class QrisVerificationResponse(
    val verdict: String,
    val action: String,
    val risk_score: Int,
    val reasons: List<String> = emptyList(),
    val signals: List<String> = emptyList(),
    val layers: Layers? = null,
    val merchant: Merchant? = null,
    val fees: FeeOut? = null,
    val verification_ticket: String? = null,
    val ticket_expires_in: Int? = null,
    val location_source: String = "",
    val device_integrity: String = "",
    /**
     * Dasar putusan ini. `null` pada jalur yang memang tidak menilai
     * jangkar: GPS yang diakui palsu, akurasi di atas ambang, dan
     * pemindaian dari gambar.
     */
    val evidence: EvidenceOut? = null,
    /**
     * Atribusi Merchant ID. Diisi HANYA pada pemindaian dari gambar —
     * di pemindaian biasa jawabannya sudah ada di `verdict`.
     */
    val known: AttributionOut? = null,
    /** Kosong kecuali `include_tlv` dinyalakan. Bentuknya selalu ada. */
    val tlv: List<TlvEntry> = emptyList(),
    val processing_ms: Double = 0.0
) {
    /**
     * Action sebagai enum yang aman dipakai di when-expression.
     * Backward compatible: kode lama yang memakai `action` (String) tetap jalan.
     */
    val verdictAction: VerdictAction
        get() = VerdictAction.fromString(action)
}
