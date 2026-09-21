package id.qshield.scanner.data.network

import id.qshield.scanner.data.models.QrisVerificationRequest
import id.qshield.scanner.data.models.QrisVerificationResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface QShieldApiService {
    @POST("api/v1/verify")
    suspend fun verifyQris(@Body request: QrisVerificationRequest): QrisVerificationResponse
}
