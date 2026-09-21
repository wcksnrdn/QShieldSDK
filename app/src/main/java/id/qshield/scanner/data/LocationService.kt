package id.qshield.scanner.data

import android.annotation.SuppressLint
import android.location.Location
import android.os.Build
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LocationService(
    private val fusedLocationClient: FusedLocationProviderClient
) {
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): LocationResult {
        val cancellationTokenSource = CancellationTokenSource()

        val location = withTimeoutOrNull(10_000L) {
            suspendCancellableCoroutine<Location?> { continuation ->
                val task = fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.token
                )
                task.addOnSuccessListener { loc ->
                    continuation.resume(loc)
                }
                task.addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
                continuation.invokeOnCancellation {
                    cancellationTokenSource.cancel()
                }
            }
        }

        if (location == null) {
            cancellationTokenSource.cancel()
            return LocationResult.Error("Lokasi tidak didapat — pastikan GPS aktif dan coba di luar ruangan")
        }

        return LocationResult.Success(
            lat = location.latitude,
            lng = location.longitude,
            accuracyM = location.accuracy,
            isMock = isMockLocation(location)
        )
    }

    private fun isMockLocation(location: Location): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            location.isMock
        } else {
            @Suppress("DEPRECATION")
            location.isFromMockProvider
        }
    }
}

sealed class LocationResult {
    data class Success(
        val lat: Double,
        val lng: Double,
        val accuracyM: Float,
        val isMock: Boolean
    ) : LocationResult()

    data class Error(val message: String) : LocationResult()
}
