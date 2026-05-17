package com.kimfamily.ledger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.kimfamily.ledger.navigation.LedgerNavHost
import com.kimfamily.ledger.ui.AppLaunchGate
import com.kimfamily.ledger.ui.theme.KyungahLedgerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val application = application as LedgerApplication
        setContent {
            var appName by remember { mutableStateOf(application.appPreferences.getAppName()) }
            KyungahLedgerTheme {
                AppLaunchGate(appName = appName) {
                    LedgerNavHost(
                        application = application,
                        appName = appName,
                        onAppNameChange = { name ->
                            application.appPreferences.setAppName(name)
                            appName = application.appPreferences.getAppName()
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}
