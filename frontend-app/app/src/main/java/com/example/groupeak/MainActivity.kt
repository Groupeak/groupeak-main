package com.example.groupeak

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.example.groupeak.service_locator.ServiceLocator
import com.example.groupeak.ui.navigation.Graph
import com.example.groupeak.ui.navigation.RootNavGraph
import com.example.groupeak.ui.startup.AppStartState
import com.example.groupeak.ui.startup.AppStartViewModel
import com.example.groupeak.ui.startup.AppStartViewModelFactory
import com.example.groupeak.ui.theme.GroupeakTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        ServiceLocator.init(applicationContext)

        val appStartViewModel = ViewModelProvider(
            this,
            AppStartViewModelFactory(
                ServiceLocator.userRepository,
                ServiceLocator.sessionManager,
            )
        )[AppStartViewModel::class.java]

        splashScreen.setKeepOnScreenCondition {
            appStartViewModel.state is AppStartState.Loading
        }

        setContent {
            GroupeakApp(appStartViewModel)
        }
    }
}

@Composable
fun GroupeakApp(appStartViewModel: AppStartViewModel) {
    GroupeakTheme {
        val navController = rememberNavController()

        Surface(color = MaterialTheme.colorScheme.background) {
            when (appStartViewModel.state) {
                AppStartState.Loading -> Unit
                AppStartState.Auth -> {
                    RootNavGraph(
                        rootNavController = navController,
                        startDestination = Graph.AUTH,
                    )
                }
                AppStartState.Main -> {
                    RootNavGraph(
                        rootNavController = navController,
                        startDestination = Graph.MAIN,
                    )
                }
            }
        }
    }
}