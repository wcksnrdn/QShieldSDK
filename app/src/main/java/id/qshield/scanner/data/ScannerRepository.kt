package id.qshield.scanner.data

import id.qshield.scanner.data.models.AmbientWifi
import id.qshield.scanner.data.models.DeviceIntegrity
import id.qshield.scanner.data.models.PrintedLabel
import id.qshield.scanner.data.models.QrisVerificationRequest
import id.qshield.scanner.data.models.QrisVerificationResponse
import id.qshield.scanner.data.network.QShieldApiService
import retrofit2.HttpException

class ScannerRepository(
    private val apiService: QShieldApiService,
    private val locationService: LocationService,
    private val wifiService: WifiService
) {
    data class VerificationResult(
        val response: QrisVerificationResponse,
        val wifiNotice: String?
    )

    suspend fun verifyQris(
        payload: String,
        deviceAnonId: String,
        printedNmid: String? = null
    ): Result<VerificationResult> {
        return try {
            val locationResult = locationService.getCurrentLocation()
            if (locationResult is LocationResult.Error) {
                return Result.failure(Exception(locationResult.message))
            }
            
            val location = locationResult as LocationResult.Success

            val wifiResult = wifiService.getWifiHashes()
            var wifiNotice: String? = null
            val ambientWifi = if (wifiResult is WifiResult.Success && wifiResult.hashes.isNotEmpty()) {
                AmbientWifi(wifiResult.hashes)
            } else if (wifiResult is WifiResult.ErrorLocationDisabled) {
                wifiNotice = wifiResult.message
                null
            } else {
                null
            }

            val deviceIntegrity = DeviceIntegrity(
                mock_location = location.isMock,
                rooted = RootDetection.isRooted()
            )
            
            val printedLabel = printedNmid?.let { PrintedLabel(nmid = it) }

            val request = QrisVerificationRequest(
                payload = payload,
                lat = location.lat,
                lng = location.lng,
                accuracy_m = location.accuracyM,
                device_anon_id = deviceAnonId,
                device_integrity = deviceIntegrity,
                ambient_wifi = ambientWifi,
                printed_label = printedLabel
            )

            val response = apiService.verifyQris(request)
            Result.success(VerificationResult(response, wifiNotice))
        } catch (e: HttpException) {
            val errorMessage = ApiErrorMapper.mapHttpError(e.code())
            Result.failure(Exception(errorMessage, e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
