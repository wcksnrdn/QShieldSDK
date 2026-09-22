package id.qshield.scanner.data.models

import kotlinx.serialization.Serializable

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

@Serializable
data class QrisVerificationResponse(
    val verdict: String,
    val action: String,
    val risk_score: Int,
    val reasons: List<String> = emptyList(),
    val signals: List<String> = emptyList(),
    val layers: Layers? = null,
    val merchant: Merchant? = null,
    val location_source: String,
    val device_integrity: String,
    val processing_ms: Double
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
