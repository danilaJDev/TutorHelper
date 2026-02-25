package by.dreb.tutorhelper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import by.dreb.tutorhelper.presentation.AppRoot
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keep system bars opaque so status area stays colored on all screens.
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = 0xFF6366F1.toInt()

        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
        }

        setContent {
            AppRoot()
        }
    }
}
