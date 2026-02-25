package by.dreb.tutorhelper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import by.dreb.tutorhelper.presentation.AppRoot
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(0xFF6366F1.toInt()),
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = 0xE6FFFFFF.toInt(),
                darkScrim = 0x801B1B1B.toInt()
            )
        )
        setContent {
            AppRoot()
        }
    }
}
