package app.slumber

import android.content.Intent
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.slumber.data.Settings
import app.slumber.ui.SlumberRoot
import app.slumber.ui.theme.SlumberTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val style = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
        if (savedInstanceState == null) opened(intent)
        setContent {
            val settings: Settings? by container.settings.settings.collectAsStateWithLifecycle(initialValue = null)
            SlumberTheme {
                // Wait for settings so the welcome screens don't flash for someone already here.
                settings?.let { SlumberRoot(it) }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        opened(intent)
    }

    /** A tap on a note or the widget: remember where it wants to go. */
    private fun opened(intent: Intent?) {
        val open = intent?.getStringExtra(EXTRA_OPEN) ?: return
        container.open.value = open to System.currentTimeMillis()
    }

    companion object {
        const val EXTRA_OPEN = "open"
        const val OPEN_CAPTURE = "capture"
        const val OPEN_CHECK = "check"
        const val OPEN_RITUAL = "ritual"
    }
}
