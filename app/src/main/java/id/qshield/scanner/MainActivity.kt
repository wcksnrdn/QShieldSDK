package id.qshield.scanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import id.qshield.scanner.ui.ScannerScreen
import id.qshield.scanner.ui.ScannerViewModel
import id.qshield.scanner.ui.theme.QShieldTheme

class MainActivity : ComponentActivity() {
    
    private val scannerViewModel: ScannerViewModel by viewModels {
        ScannerViewModel.Factory(this.applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QShieldTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ScannerScreen(viewModel = scannerViewModel)
                }
            }
        }
    }
}
