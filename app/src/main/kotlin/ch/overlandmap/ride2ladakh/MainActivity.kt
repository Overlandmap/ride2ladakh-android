package ch.overlandmap.ride2ladakh

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import ch.overlandmap.map.AppMode
import ch.overlandmap.map.ui.theme.OverlandTheme

// AppCompatActivity (not ComponentActivity) so per-app locales work below API 33.
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Single-track-pack app: the shared code resolves this pack by name.
        AppMode.singleTrackPack = true
        AppMode.trackPackName = LADAKH_PACK_NAME
        setContent {
            OverlandTheme {
                Ride2LadakhNavigation()
            }
        }
    }
}
