package id.qshield.scanner.data.models

/**
 * State machine untuk alur navigasi UI QShield.
 *
 * Setiap state merepresentasikan satu layar atau kondisi dalam alur
 * pemindaian → verifikasi → keputusan pengguna. State-nya linear:
 *
 *   Launcher → Scanning → Verifying → Verdict
 *                                       ├─ proceed  → Success
 *                                       ├─ warn     → Verdict (tampilkan alasan, bisa lanjut)
 *                                       ├─ step_up  → StepUpConfirmation → PinEntry → Success
 *                                       └─ cooling_off → CoolingOff (countdown, tidak bisa lanjut)
 *
 * ErrorState bisa dicapai dari state mana pun.
 */
sealed interface QShieldFlowState {

    /** Layar awal — belum memindai apa pun. */
    data object Launcher : QShieldFlowState

    /** Kamera aktif, menunggu QR terbaca. */
    data object Scanning : QShieldFlowState

    /** Payload dikirim ke server, menunggu tanggapan. */
    data object Verifying : QShieldFlowState

    /** Putusan diterima — tampilkan verdict dengan merchant info dan alasan. */
    data class Verdict(
        val response: QrisVerificationResponse,
        val rawPayload: String
    ) : QShieldFlowState

    /** action=step_up: tampilkan konfirmasi "apakah Anda yakin?" */
    data class StepUpConfirmation(
        val response: QrisVerificationResponse,
        val rawPayload: String
    ) : QShieldFlowState

    /** action=cooling_off: countdown timer, pengguna TIDAK bisa melanjutkan. */
    data class CoolingOff(
        val response: QrisVerificationResponse,
        val remainingSeconds: Int
    ) : QShieldFlowState

    /** Verifikasi PIN setelah step_up dikonfirmasi. */
    data class PinEntry(
        val response: QrisVerificationResponse,
        val rawPayload: String
    ) : QShieldFlowState

    /** Transaksi selesai — tampilkan konfirmasi sukses. */
    data class Success(val merchantName: String) : QShieldFlowState

    /** Kesalahan — tampilkan pesan dengan opsi coba lagi. */
    data class ErrorState(
        val title: String,
        val message: String,
        val canRetry: Boolean = true
    ) : QShieldFlowState
}
