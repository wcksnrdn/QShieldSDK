package id.qshield.scanner.data

import id.qshield.scanner.data.local.PreferencesDataStore
import id.qshield.scanner.data.local.SumberLokasi
import id.qshield.scanner.data.models.AmbientWifi
import id.qshield.scanner.data.models.InspectRequest
import id.qshield.scanner.data.models.InspectResponse
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

    /**
     * Katalogkan satu payload. Tidak mengambil lokasi, tidak mengambil
     * WiFi, tidak melaporkan integritas — tidak ada satu pun dari itu
     * yang relevan untuk pertanyaan "payload ini isinya apa".
     */
    suspend fun inspectQris(payload: String): Result<InspectResponse> {
        return try {
            Result.success(apiService.inspect(InspectRequest(payload = payload)))
        } catch (e: HttpException) {
            Result.failure(Exception(ApiErrorMapper.mapHttpError(e.code()), e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyQris(
        payload: String,
        deviceAnonId: String,
        printedNmid: String? = null,
        sumber: SumberLokasi = SumberLokasi(),
    ): Result<VerificationResult> {
        return try {
            var wifiNotice: String? = null

            val lat: Double
            val lng: Double
            val accuracyM: Float
            val ambientWifi: AmbientWifi?
            val isMock: Boolean

            if (sumber.replay) {
                // Koordinat dari rekaman. GPS TIDAK dibaca sama sekali —
                // membacanya lalu membuangnya hanya menambah penundaan
                // dan bisa gagal justru di tempat mode ini dipakai.
                lat = sumber.lat!!
                lng = sumber.lng!!
                accuracyM = sumber.accuracyM ?: 10f
                isMock = false

                // Sidik jari WiFi SENGAJA tidak dikirim.
                //
                // Titik akses yang terbaca adalah milik gedung tempat
                // kita berdiri sekarang, bukan milik tempat yang sedang
                // diputar ulang. Mengirimkannya membuat server
                // membandingkan WiFi ruang demo dengan WiFi warung, lalu
                // menemukan ketidakcocokan yang memang seharusnya ada —
                // dan merchant yang sah turun dari hijau tepat di depan
                // juri. Replay berarti kita tidak punya sinyal sekitar
                // yang jujur dari sana; maka tidak ada yang dikirim.
                ambientWifi = null
            } else {
                val locationResult = locationService.getCurrentLocation()
                if (locationResult is LocationResult.Error) {
                    return Result.failure(Exception(locationResult.message))
                }
                val location = locationResult as LocationResult.Success
                lat = location.lat
                lng = location.lng
                accuracyM = location.accuracyM
                isMock = location.isMock

                val wifiResult = wifiService.getWifiHashes()
                ambientWifi = if (wifiResult is WifiResult.Success &&
                    wifiResult.hashes.isNotEmpty()
                ) {
                    AmbientWifi(wifiResult.hashes)
                } else if (wifiResult is WifiResult.ErrorLocationDisabled) {
                    wifiNotice = wifiResult.message
                    null
                } else {
                    null
                }
            }

            val deviceIntegrity = DeviceIntegrity(
                mock_location = isMock,
                rooted = RootDetection.isRooted()
            )

            val printedLabel = printedNmid?.let { PrintedLabel(nmid = it) }

            val request = QrisVerificationRequest(
                payload = payload,
                lat = lat,
                lng = lng,
                accuracy_m = accuracyM,
                device_anon_id = deviceAnonId,
                device_integrity = deviceIntegrity,
                ambient_wifi = ambientWifi,
                printed_label = printedLabel,
                location_source = if (sumber.replay) {
                    PreferencesDataStore.SUMBER_REPLAY
                } else {
                    PreferencesDataStore.SUMBER_LIVE
                },
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
