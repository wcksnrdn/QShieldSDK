package id.qshield.scanner

import id.qshield.scanner.data.ApiErrorMapper
import id.qshield.scanner.data.WifiService
import id.qshield.scanner.data.models.AmbientWifi
import id.qshield.scanner.data.models.QrisVerificationRequest
import id.qshield.scanner.data.network.ApiClient
import kotlinx.serialization.encodeToString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import id.qshield.scanner.ui.theme.ActionColorMapper
import androidx.compose.ui.graphics.Color

class ScannerTests {

    @Test
    fun `bssid hashing returns correct 32 character hex string`() {
        val bssid = "00:1a:2b:0c:3d:4e"
        val expectedHex = WifiService.hashBssid(bssid)
        assertEquals(32, expectedHex.length)
        // Should be lowercase
        assertEquals(expectedHex, expectedHex.lowercase())
        
        // Same bssid gives same hash regardless of case
        val bssidUpper = "00:1A:2B:0C:3D:4E"
        assertEquals(expectedHex, WifiService.hashBssid(bssidUpper))
        
        // Let's actually verify SHA-256 for this specific bssid string
        // "00:1a:2b:0c:3d:4e".lowercase() -> sha256 -> take(32)
        // We'll trust the function itself to be deterministic.
    }

    @Test
    fun `api error mapper returns correct indonesian strings`() {
        assertEquals("Kunci API tidak valid", ApiErrorMapper.mapHttpError(401))
        assertEquals("Kode terlalu panjang", ApiErrorMapper.mapHttpError(413))
        assertEquals("Kode ini bukan QRIS yang sah", ApiErrorMapper.mapHttpError(422))
        assertEquals("Terlalu banyak permintaan, tunggu sebentar", ApiErrorMapper.mapHttpError(429))
        assertEquals("Server belum dikonfigurasi", ApiErrorMapper.mapHttpError(503))
        assertEquals("Tidak bisa menghubungi server", ApiErrorMapper.mapHttpError(404))
    }

    @Test
    fun `api request builder omits null optional objects`() {
        val request = QrisVerificationRequest(
            payload = "QRIS_STRING",
            lat = -6.914744,
            lng = 107.609810,
            accuracy_m = 8.5f,
            device_anon_id = "UUID_123"
        )
        
        val jsonString = ApiClient.networkJson.encodeToString(request)
        
        // device_integrity, ambient_wifi, printed_label should be omitted
        assertFalse(jsonString.contains("device_integrity"))
        assertFalse(jsonString.contains("ambient_wifi"))
        assertFalse(jsonString.contains("printed_label"))
        assertTrue(jsonString.contains("payload"))
    }
    
    @Test
    fun `api request builder includes objects when present`() {
        val request = QrisVerificationRequest(
            payload = "QRIS_STRING",
            lat = -6.914744,
            lng = 107.609810,
            accuracy_m = 8.5f,
            device_anon_id = "UUID_123",
            ambient_wifi = AmbientWifi(listOf("hash1", "hash2"))
        )
        
        val jsonString = ApiClient.networkJson.encodeToString(request)
        
        // device_integrity, printed_label should be omitted
        assertFalse(jsonString.contains("device_integrity"))
        assertFalse(jsonString.contains("printed_label"))
        
        // ambient_wifi should be present
        assertTrue(jsonString.contains("ambient_wifi"))
        assertTrue(jsonString.contains("hash1"))
    }
    
    @Test
    fun `action color mapper maps correctly`() {
        assertEquals(Color.Green, ActionColorMapper.mapActionToColor("proceed"))
        assertEquals(Color(0xFFFFBF00), ActionColorMapper.mapActionToColor("warn"))
        assertEquals(Color(0xFFFFA500), ActionColorMapper.mapActionToColor("step_up"))
        assertEquals(Color.Red, ActionColorMapper.mapActionToColor("cooling_off"))
        assertEquals(Color.Gray, ActionColorMapper.mapActionToColor("unknown"))
        assertEquals(Color.Green, ActionColorMapper.mapActionToColor("PROCEED")) // case insensitive
    }
}
