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
