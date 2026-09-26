package id.qshield.scanner.ui.theme

import androidx.compose.ui.graphics.Color
import id.qshield.scanner.data.models.VerdictAction

object ActionColorMapper {

    /** Warna berdasarkan VerdictAction enum — yang baru dan dianjurkan. */
    fun mapActionToColor(action: VerdictAction): Color {
        return when (action) {
            VerdictAction.PROCEED -> Color(0xFF22C08A)     // hijau — sesuai web frontend
            VerdictAction.WARN -> Color(0xFFF0A82C)        // amber
            VerdictAction.STEP_UP -> Color(0xFFF0742C)     // oranye
            VerdictAction.COOLING_OFF -> Color(0xFFE5484D) // merah
            VerdictAction.UNKNOWN -> Color.Gray
        }
    }

    /** Backward compat — kode lama yang masih memegang action sebagai String. */
    fun mapActionToColor(action: String): Color {
        return mapActionToColor(VerdictAction.fromString(action))
    }
}
