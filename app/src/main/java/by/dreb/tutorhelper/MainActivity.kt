package by.dreb.tutorhelper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import by.dreb.tutorhelper.data.seed.SampleDataSeeder
import by.dreb.tutorhelper.presentation.AppRoot
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var sampleDataSeeder: SampleDataSeeder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        sampleDataSeeder.seedIfEmpty()
        setContent {
            AppRoot()
        }
    }
}
