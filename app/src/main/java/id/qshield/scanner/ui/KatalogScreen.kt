package id.qshield.scanner.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.qshield.scanner.data.models.InspectResponse

/**
 * Hasil mode katalog.
 *
 * SENGAJA tidak berwarna hijau/merah dan tidak menyebut aman atau tidak.
 * Tidak ada lokasi yang dikirim, jadi tidak ada putusan lokasi yang bisa
 * diberikan — dan tampilan yang terlihat seperti putusan akan membuat
 * pemakainya mengira QR ini sudah diperiksa tempatnya. Belum.
 */
@Composable
fun KatalogScreen(hasil: InspectResponse, onRestart: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Tercatat ke korpus", style = MaterialTheme.typography.titleLarge)
                Text(
                    hasil.merchant?.name ?: "(nama merchant tidak terbaca)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    hasil.merchant?.nmid ?: "-",
                    style = MaterialTheme.typography.bodyMedium
                )
                hasil.merchant?.city?.let {
                    Text("Kota di stiker: $it", style = MaterialTheme.typography.bodySmall)
                }

                HorizontalDivider()

                if (hasil.structural_reasons.isEmpty()) {
                    Text(
                        "Bentuk payload wajar — tidak ada kejanggalan struktural.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text(
                        "Kejanggalan pada bentuk payload:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    hasil.structural_reasons.forEach {
                        Text("• $it", style = MaterialTheme.typography.bodySmall)
                    }
                }

                HorizontalDivider()

                Text(
                    "Lokasi TIDAK diperiksa. Mode katalog hanya membaca isi " +
                        "payload — gunakan mode Verifikasi saat berdiri di " +
                        "depan stiker yang sesungguhnya.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Button(onClick = onRestart, modifier = Modifier.fillMaxWidth()) {
            Text("Pindai lagi")
        }
    }
}
