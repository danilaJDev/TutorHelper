package by.dreb.tutorhelper.presentation.common

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Общая палитра для экранов приложения в стиле "Расписание".
 */
object AppPalette {
    val Background = Color(0xFFF8FAFC)
    val Surface = Color(0xFFFFFFFF)
    val Primary = Color(0xFF6366F1)
    val TextPrimary = Color(0xFF1E293B)
    val TextSecondary = Color(0xFF64748B)

    val Success = Color(0xFF22C55E)
    val Action = Color(0xFFF59E0B)
    val Error = Color(0xFFEF4444)

    val CardElevation = 2.dp
    val CardShape = RoundedCornerShape(16.dp)
}
