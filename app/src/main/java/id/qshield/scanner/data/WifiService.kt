package id.qshield.scanner.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import android.net.wifi.WifiManager
import androidx.core.location.LocationManagerCompat
import java.security.MessageDigest

class WifiService(private val context: Context) {
    private var cachedHashes: List<String> = emptyList()
    private var lastScanTime: Long = 0

    @SuppressLint("MissingPermission")
    fun getWifiHashes(): WifiResult {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!LocationManagerCompat.isLocationEnabled(locationManager)) {
            return WifiResult.ErrorLocationDisabled()
        }

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastScanTime < 60_000 && cachedHashes.isNotEmpty()) {
            return WifiResult.Success(cachedHashes)
        }

        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        
        return try {
            val scanResults = wifiManager.scanResults
            val hashes = scanResults
                .sortedByDescending { it.level }
                .take(32)
                .map { hashBssid(it.BSSID) }

            cachedHashes = hashes
            lastScanTime = currentTime

            WifiResult.Success(hashes)
        } catch (e: SecurityException) {
            // Need permissions
            WifiResult.Success(emptyList())
        }
    }

    companion object {
        fun hashBssid(bssid: String): String {
            val normalised = bssid.lowercase()
            val digest = MessageDigest.getInstance("SHA-256")
                .digest(normalised.toByteArray())
            return digest.joinToString("") { "%02x".format(it) }.take(32)
        }
    }
}

sealed class WifiResult {
    data class Success(val hashes: List<String>) : WifiResult()
    data class ErrorLocationDisabled(
        val message: String = "Sidik jari WiFi tidak terkumpul — layanan lokasi mati"
    ) : WifiResult()
}
