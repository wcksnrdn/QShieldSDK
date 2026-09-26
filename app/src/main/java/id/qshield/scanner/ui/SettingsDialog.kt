package id.qshield.scanner.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.LocationServices
import id.qshield.scanner.data.LocationResult
import id.qshield.scanner.data.LocationService
import id.qshield.scanner.data.WifiResult
import id.qshield.scanner.data.WifiService
import id.qshield.scanner.data.local.PreferencesDataStore
import id.qshield.scanner.data.local.SumberLokasi
import kotlinx.coroutines.launch

/**
 * Ambang invarian §6. Di atas ini server MENOLAK memberi putusan
 * lokasi, jadi merekam titik dengan akurasi seburuk itu menghasilkan
 * rekaman yang tidak berguna — dan itu baru ketahuan saat demo.
 */
private const val AMBANG_AKURASI_M = 100f

@Composable
fun SettingsDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val dataStore = remember { PreferencesDataStore(context) }
    val locationService = remember {
        LocationService(LocationServices.getFusedLocationProviderClient(context))
    }
    val wifiService = remember { WifiService(context) }
    val scope = rememberCoroutineScope()

    val baseUrl by dataStore.baseUrlFlow.collectAsState(initial = "")
    val apiKey by dataStore.apiKeyFlow.collectAsState(initial = "")
    val sumber by dataStore.sumberLokasiFlow.collectAsState(initial = SumberLokasi())

    var editBaseUrl by remember(baseUrl) { mutableStateOf(baseUrl) }
    var editApiKey by remember(apiKey) { mutableStateOf(apiKey) }
    var editLat by remember(sumber.lat) { mutableStateOf(sumber.lat?.toString() ?: "") }
    var editLng by remember(sumber.lng) { mutableStateOf(sumber.lng?.toString() ?: "") }

    // Pembacaan GPS/WiFi terakhir, ditampilkan apa adanya.
    var bacaan by remember { mutableStateOf<LocationResult?>(null) }
    var jumlahAp by remember { mutableStateOf<Int?>(null) }
    var sedangMembaca by remember { mutableStateOf(false) }
    var pesan by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pengaturan") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                JudulBagian("Server")
                OutlinedTextField(
                    value = editBaseUrl,
                    onValueChange = { editBaseUrl = it },
                    label = { Text("Base URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = editApiKey,
                    onValueChange = { editApiKey = it },
                    label = { Text("API Key") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(20.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))

                JudulBagian("Sumber lokasi")
                Text(
                    "GPS di dalam gedung sering melaporkan akurasi ratusan " +
                        "meter, dan di titik itu server menolak memberi putusan " +
                        "lokasi. Rekam titik demo di LUAR gedung saat akurasinya " +
                        "masih bagus, lalu putar ulang di dalam.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))

                Column(Modifier.selectableGroup()) {
                    PilihanSumber(
                        judul = "Live — GPS langsung",
                        keterangan = "Dipakai di lapangan. Sidik jari WiFi ikut terkirim.",
                        dipilih = !sumber.replay,
                        onPilih = { scope.launch { dataStore.saveSumberLokasi(false) } },
                    )
                    PilihanSumber(
                        judul = "Replay — koordinat rekaman",
                        keterangan = if (sumber.punyaTitik) {
                            "Ditandai terbuka di tanggapan dan jejak audit."
                        } else {
                            "Rekam titiknya dulu di bawah."
                        },
                        dipilih = sumber.replay,
                        aktif = sumber.punyaTitik,
                        onPilih = { scope.launch { dataStore.saveSumberLokasi(true) } },
                    )
                }

                Spacer(Modifier.height(12.dp))

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            "Pembacaan perangkat",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Spacer(Modifier.height(6.dp))
                        when (val b = bacaan) {
                            null -> Text(
                                "Belum dibaca.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )

                            is LocationResult.Error -> Text(
                                b.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )

                            is LocationResult.Success -> {
                                Text(
                                    "%.6f, %.6f".format(b.lat, b.lng),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = FontFamily.Monospace,
                                )
                                Text(
                                    "akurasi ±%.0f m".format(b.accuracyM),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (b.accuracyM > AMBANG_AKURASI_M) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                )
                                if (b.accuracyM > AMBANG_AKURASI_M) {
                                    Text(
                                        "Di atas ambang ${AMBANG_AKURASI_M.toInt()} m — " +
                                            "server akan menolak memberi putusan lokasi. " +
                                            "Jangan direkam dari sini.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error,
                                    )
                                }
                                if (b.isMock) {
                                    Text(
                                        "Sistem melaporkan lokasi ini dari mock provider.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error,
                                    )
                                }
                                jumlahAp?.let {
                                    Text(
                                        "$it titik akses WiFi terbaca",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        enabled = !sedangMembaca,
                        onClick = {
                            scope.launch {
                                sedangMembaca = true
                                pesan = null
                                bacaan = locationService.getCurrentLocation()
                                jumlahAp = (wifiService.getWifiHashes()
                                    as? WifiResult.Success)?.hashes?.size
                                sedangMembaca = false
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text(if (sedangMembaca) "Membaca…" else "Baca GPS") }

                    Button(
                        enabled = bacaan is LocationResult.Success && !sedangMembaca,
                        onClick = {
                            val b = bacaan as? LocationResult.Success ?: return@Button
                            scope.launch {
                                dataStore.saveTitikReplay(b.lat, b.lng, b.accuracyM)
                                editLat = b.lat.toString()
                                editLng = b.lng.toString()
                                pesan = if (b.accuracyM > AMBANG_AKURASI_M) {
                                    "Tersimpan, TAPI akurasinya buruk. " +
                                        "Rekam ulang di luar gedung."
                                } else {
                                    "Titik tersimpan."
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Rekam titik ini") }
                }

                Spacer(Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editLat,
                        onValueChange = { editLat = it },
                        label = { Text("Lintang") },
                        placeholder = { Text("-6.168580") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = editLng,
                        onValueChange = { editLng = it },
                        label = { Text("Bujur") },
                        placeholder = { Text("106.872458") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                sumber.recordedAt?.let { waktu ->
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Direkam $waktu" +
                            (sumber.accuracyM?.let { a -> "  (±%.0f m)".format(a) } ?: ""),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                pesan?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                if (sumber.punyaTitik) {
                    Spacer(Modifier.height(4.dp))
                    TextButton(onClick = {
                        scope.launch {
                            dataStore.hapusTitikReplay()
                            editLat = ""
                            editLng = ""
                            pesan = "Titik dihapus, kembali ke Live."
                        }
                    }) { Text("Hapus titik rekaman") }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch {
                        dataStore.saveBaseUrl(editBaseUrl)
                        dataStore.saveApiKey(editApiKey)
                        // Koordinat ketikan ikut disimpan, tapi hanya
                        // kalau DUA-DUANYA angka yang sah — setengah
                        // koordinat lebih buruk daripada tidak ada.
                        val lat = editLat.trim().toDoubleOrNull()
                        val lng = editLng.trim().toDoubleOrNull()
                        if (lat != null && lng != null) {
                            dataStore.saveTitikReplay(lat, lng, null)
                        }
                        onDismiss()
                    }
                }
            ) { Text("Simpan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
private fun JudulBagian(teks: String) {
    Text(
        teks,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
    )
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun PilihanSumber(
    judul: String,
    keterangan: String,
    dipilih: Boolean,
    onPilih: () -> Unit,
    aktif: Boolean = true,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        RadioButton(selected = dipilih, onClick = onPilih, enabled = aktif)
        Column(Modifier.padding(start = 4.dp)) {
            Text(
                judul,
                style = MaterialTheme.typography.bodyMedium,
                color = if (aktif) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
            Text(
                keterangan,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
