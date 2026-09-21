package id.qshield.scanner.ui


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.qshield.scanner.data.models.QrisVerificationResponse
import id.qshield.scanner.ui.theme.ActionColorMapper

@Composable
fun ResultScreen(
    result: QrisVerificationResponse,
    wifiNotice: String?,
    currentPayload: String,
    onVerifyPrintedLabel: (String, String) -> Unit,
    onRestart: () -> Unit
) {
    val scrollState = rememberScrollState()
    var printedNmid by remember { mutableStateOf("") }
    
    val color = ActionColorMapper.mapActionToColor(result.action)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Verdict: ${result.verdict.uppercase()}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (result.merchant?.name != null) {
                    Text("Merchant: ${result.merchant.name}", style = MaterialTheme.typography.titleMedium)
                }
                if (result.merchant?.nmid != null) {
                    Text("NMID: ${result.merchant.nmid}", style = MaterialTheme.typography.titleMedium)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (result.reasons.isNotEmpty()) {
                    Text("Alasan:", fontWeight = FontWeight.Bold)
                    result.reasons.forEach { reason ->
                        Text("• $reason", modifier = Modifier.padding(start = 8.dp))
                    }
                }
                
                if (wifiNotice != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = wifiNotice,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text("Cocokkan dengan Merchant ID yang tercetak di stiker", style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = printedNmid,
                    onValueChange = { printedNmid = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Printed NMID") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onVerifyPrintedLabel(currentPayload, printedNmid) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Periksa")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onRestart,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Pindai lagi")
                }
            }
        }
    }
}
