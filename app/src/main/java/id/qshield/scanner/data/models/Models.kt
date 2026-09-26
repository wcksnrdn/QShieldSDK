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

// ── Request models (unchanged) ───────────────────────────────────

@Serializable
data class QrisVerificationRequest(
    val payload: String,
    val lat: Double,
    val lng: Double,
    val accuracy_m: Float,
    val device_anon_id: String,
    val device_integrity: DeviceIntegrity? = null,
    val ambient_wifi: AmbientWifi? = null,
    val printed_label: PrintedLabel? = null
)

@Serializable
data class DeviceIntegrity(
    val mock_location: Boolean,
    val rooted: Boolean,
    val attested: String? = null,
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
    val structural_signals: List<String> = emptyList(),
    val structural_reasons: List<String> = emptyList(),
    val processing_ms: Double = 0.0
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
    val processing_ms: Double = 0.0
) {
    /**
     * Action sebagai enum yang aman dipakai di when-expression.
     * Backward compatible: kode lama yang memakai `action` (String) tetap jalan.
     */
    val verdictAction: VerdictAction
        get() = VerdictAction.fromString(action)
}
