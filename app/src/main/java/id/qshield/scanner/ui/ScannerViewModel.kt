package id.qshield.scanner.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import id.qshield.scanner.data.LocationService
import id.qshield.scanner.data.ScannerRepository
import id.qshield.scanner.data.WifiService
import id.qshield.scanner.data.local.PreferencesDataStore
import id.qshield.scanner.data.models.QShieldFlowState
import id.qshield.scanner.data.models.QrisVerificationResponse
import id.qshield.scanner.data.models.VerdictAction
import id.qshield.scanner.data.network.ApiClient
import id.qshield.scanner.data.network.QShieldApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * ViewModel yang mengendalikan alur QShield sepenuhnya lewat
 * [QShieldFlowState]. Setiap transisi state didasarkan pada `action`
 * yang dikembalikan backend.
 *
 * Alur:
 *   Scanning → Verifying → (backend response) →
 *     proceed    → Verdict → Success
 *     warn       → Verdict → (user proceeds) → Success
 *     step_up    → StepUpConfirmation → (confirm) → PinEntry → Success
 *     cooling_off→ CoolingOff (countdown, no proceed)
 */
class ScannerViewModel(
    private val repository: ScannerRepository,
    private val prefs: PreferencesDataStore
) : ViewModel() {

    private val _flowState = MutableStateFlow<QShieldFlowState>(QShieldFlowState.Scanning)
    val flowState: StateFlow<QShieldFlowState> = _flowState.asStateFlow()

    /**
     * Mode katalog. Saat menyala, pemindaian dikirim ke /inspect —
     * membaca isi payload tanpa menyentuh pengetahuan lokasi.
     */
    private val _modeKatalog = MutableStateFlow(false)
    val modeKatalog: StateFlow<Boolean> = _modeKatalog.asStateFlow()

    fun setModeKatalog(aktif: Boolean) { _modeKatalog.value = aktif }

    // ── Scan entry point ─────────────────────────────────────────

    fun onQrCodeScanned(payload: String) {
        // Prevent multiple scans
        val current = _flowState.value
        if (current !is QShieldFlowState.Scanning && current !is QShieldFlowState.Launcher) return

        if (_modeKatalog.value) {
            katalogkan(payload)
        } else {
            verify(payload)
        }
    }

    private fun katalogkan(payload: String) {
        viewModelScope.launch {
            _flowState.value = QShieldFlowState.Verifying
            repository.inspectQris(payload)
                .onSuccess {
                    // Katalog tidak punya verdict — langsung kembali ke scanning
                    // dengan info yang sudah ditampilkan lewat snackbar/toast
                    _flowState.value = QShieldFlowState.Scanning
                }
                .onFailure {
                    _flowState.value = QShieldFlowState.ErrorState(
                        title = "Error",
                        message = it.message ?: "Terjadi kesalahan"
                    )
                }
        }
    }

    private fun verify(payload: String) {
        viewModelScope.launch {
            _flowState.value = QShieldFlowState.Verifying

            val deviceAnonId = prefs.getOrGenerateUuid()
            // Dibaca sekali per pemindaian, bukan disimpan di ViewModel:
            // pengguna bisa mengubahnya di dialog Setelan di tengah sesi,
            // dan pemindaian berikutnya harus langsung memakai yang baru.
            val sumber = prefs.sumberLokasiFlow.first()
            val result = repository.verifyQris(payload, deviceAnonId, null, sumber)

            result.onSuccess { res ->
                routeVerdict(res.response, payload)
            }.onFailure { error ->
                _flowState.value = QShieldFlowState.ErrorState(
                    title = "Verification Failed",
                    message = error.message ?: "Terjadi kesalahan"
                )
            }
        }
    }

    /**
     * Rute putusan berdasarkan action dari backend.
     *
     * Pemetaan ini COCOK PERSIS dengan binding.py:
     *   proceed    → Verdict (hijau, bisa langsung lanjut)
     *   warn       → Verdict (kuning, bisa lanjut setelah baca alasan)
     *   step_up    → StepUpConfirmation (oranye, perlu konfirmasi + PIN)
     *   cooling_off→ CoolingOff (merah, TIDAK bisa lanjut)
     */
    private fun routeVerdict(response: QrisVerificationResponse, payload: String) {
        when (response.verdictAction) {
            VerdictAction.PROCEED -> {
                _flowState.value = QShieldFlowState.Verdict(
                    response = response,
                    rawPayload = payload
                )
            }
            VerdictAction.WARN -> {
                _flowState.value = QShieldFlowState.Verdict(
                    response = response,
                    rawPayload = payload
                )
            }
            VerdictAction.STEP_UP -> {
                _flowState.value = QShieldFlowState.StepUpConfirmation(
                    response = response,
                    rawPayload = payload
                )
            }
            VerdictAction.COOLING_OFF -> {
                _flowState.value = QShieldFlowState.CoolingOff(
                    response = response,
                    remainingSeconds = COOLING_OFF_SECONDS
                )
            }
            VerdictAction.UNKNOWN -> {
                // Aksi tidak dikenal — perlakukan sebagai warn
                _flowState.value = QShieldFlowState.Verdict(
                    response = response,
                    rawPayload = payload
                )
            }
        }
    }

    // ── Flow transition actions ──────────────────────────────────

    /** Pengguna menekan Proceed di layar Verdict (proceed/warn). */
    fun onVerdictProceed() {
        val state = _flowState.value
        if (state is QShieldFlowState.Verdict) {
            _flowState.value = QShieldFlowState.Success(
                merchantName = state.response.merchant?.name ?: "Merchant"
            )
        }
    }

    /** Pengguna mengkonfirmasi step-up → masuk ke PIN entry. */
    fun onStepUpConfirmed() {
        val state = _flowState.value
        if (state is QShieldFlowState.StepUpConfirmation) {
            _flowState.value = QShieldFlowState.PinEntry(
                response = state.response,
                rawPayload = state.rawPayload
            )
        }
    }

    /** PIN dimasukkan — verifikasi (untuk demo, langsung sukses). */
    fun onPinEntered(pin: String) {
        val state = _flowState.value
        if (state is QShieldFlowState.PinEntry) {
            // TODO: Verifikasi PIN dengan backend jika diperlukan
            _flowState.value = QShieldFlowState.Success(
                merchantName = state.response.merchant?.name ?: "Merchant"
            )
        }
    }

    /** Kembali ke pemindaian — dari state mana pun. */
    fun restartScanning() {
        _flowState.value = QShieldFlowState.Scanning
    }

    companion object {
        /** Durasi cooling-off dalam detik. */
        private const val COOLING_OFF_SECONDS = 30
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val prefs = PreferencesDataStore(context)
            val okHttpClient = ApiClient.createOkHttpClient(prefs)
            val retrofit = ApiClient.createRetrofit(okHttpClient)
            val apiService = retrofit.create(QShieldApiService::class.java)

            val locationService = LocationService(LocationServices.getFusedLocationProviderClient(context))
            val wifiService = WifiService(context)

            val repository = ScannerRepository(apiService, locationService, wifiService)

            return ScannerViewModel(repository, prefs) as T
        }
    }
}
