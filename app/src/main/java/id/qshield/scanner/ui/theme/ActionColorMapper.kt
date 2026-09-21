package id.qshield.scanner.ui.theme

import androidx.compose.ui.graphics.Color

object ActionColorMapper {
    fun mapActionToColor(action: String): Color {
        return when (action.lowercase()) {
            "proceed" -> Color.Green
            "warn" -> Color(0xFFFFBF00) // Amber
            "step_up" -> Color(0xFFFFA500) // Orange
            "cooling_off" -> Color.Red
            else -> Color.Gray
        }
    }
}
