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
import id.qshield.scanner.data.models.InspectResponse
import id.qshield.scanner.data.models.QrisVerificationResponse
import id.qshield.scanner.data.network.ApiClient
import id.qshield.scanner.data.network.QShieldApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ScannerUiState {
    object Scanning : ScannerUiState()
    
    data class Result(
        val response: QrisVerificationResponse,
        val wifiNotice: String? = null,
        val currentPayload: String
    ) : ScannerUiState()
    
    /** Hasil katalog — tidak ada putusan lokasi, karena tidak ada lokasi. */
    data class Katalog(val response: InspectResponse) : ScannerUiState()

    data class Error(val message: String) : ScannerUiState()
}

class ScannerViewModel(
    private val repository: ScannerRepository,
    private val prefs: PreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScannerUiState>(ScannerUiState.Scanning)
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    // Pengenal perangkat WAJIB bertahan antar peluncuran. Versi sebelumnya
    // membangkitkannya di sini, di memori, sehingga tiap kali aplikasi dibuka
    // server melihat perangkat baru.
    //
    // Itu bukan sekadar rapi-rapi: MIN_OBSERVERS = 3 adalah inti pertahanan
    // Q-Shield, dan dengan UUID yang lahir ulang tiap peluncuran satu orang
    // dengan satu HP memenuhinya cukup dengan menutup-buka aplikasi tiga
    // kali. Konsensus yang seharusnya mahal jadi gratis.
    //
    // getOrGenerateUuid() atomik lewat DataStore.edit, jadi dua pemindaian
    // yang berbarengan tidak bisa menghasilkan dua pengenal berbeda.

    /**
     * Mode katalog. Saat menyala, pemindaian dikirim ke /inspect —
     * membaca isi payload tanpa menyentuh pengetahuan lokasi.
     *
     * Ada karena mengkatalogkan QRIS lewat /verify menandai jangkar di
     * sekitar pemindai sebagai berkali-kali diserang, dan pedagang
     * sungguhan di sekitarnya ikut tertuduh.
     */
    private val _modeKatalog = MutableStateFlow(false)
    val modeKatalog: StateFlow<Boolean> = _modeKatalog.asStateFlow()

    fun setModeKatalog(aktif: Boolean) { _modeKatalog.value = aktif }

    fun onQrCodeScanned(payload: String) {
        // Prevent multiple scans
        if (_uiState.value !is ScannerUiState.Scanning) return
        if (_modeKatalog.value) katalogkan(payload) else verify(payload, null)
    }

    private fun katalogkan(payload: String) {
        viewModelScope.launch {
            repository.inspectQris(payload)
                .onSuccess { _uiState.value = ScannerUiState.Katalog(it) }
                .onFailure {
                    _uiState.value = ScannerUiState.Error(
                        it.message ?: "Terjadi kesalahan")
                }
        }
    }
    
    fun onVerifyPrintedLabel(payload: String, printedNmid: String) {
        verify(payload, printedNmid)
    }

    private fun verify(payload: String, printedNmid: String?) {
        viewModelScope.launch {
            val deviceAnonId = prefs.getOrGenerateUuid()
            val result = repository.verifyQris(payload, deviceAnonId, printedNmid)
            result.onSuccess { res ->
                _uiState.value = ScannerUiState.Result(
                    response = res.response,
                    wifiNotice = res.wifiNotice,
                    currentPayload = payload
                )
            }.onFailure { error ->
                _uiState.value = ScannerUiState.Error(
                    message = error.message ?: "Terjadi kesalahan"
                )
            }
        }
    }
    
    fun restartScanning() {
        _uiState.value = ScannerUiState.Scanning
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
