package id.qshield.scanner.data.network

import id.qshield.scanner.data.models.InspectRequest
import id.qshield.scanner.data.models.InspectResponse
import id.qshield.scanner.data.models.QrisVerificationRequest
import id.qshield.scanner.data.models.QrisVerificationResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface QShieldApiService {
    @POST("api/v1/verify")
    suspend fun verifyQris(@Body request: QrisVerificationRequest): QrisVerificationResponse

    /** Katalog: baca isi payload tanpa mengirim lokasi apa pun. */
    @POST("api/v1/inspect")
    suspend fun inspect(@Body request: InspectRequest): InspectResponse
}
