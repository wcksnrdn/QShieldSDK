package id.qshield.scanner.data

object ApiErrorMapper {
    fun mapHttpError(code: Int): String {
        return when (code) {
            401 -> "Kunci API tidak valid"
            413 -> "Kode terlalu panjang"
            422 -> "Kode ini bukan QRIS yang sah"
            429 -> "Terlalu banyak permintaan, tunggu sebentar"
            503 -> "Server belum dikonfigurasi"
            else -> "Tidak bisa menghubungi server"
        }
    }
}
