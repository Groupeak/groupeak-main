package com.example.groupeak

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.groupeak.service_locator.ServiceLocator
import com.example.groupeak.ui.navigation.RootNavGraph
import com.example.groupeak.ui.theme.GroupeakTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Важно: инициализируем зависимости ДО setContent
        ServiceLocator.init(applicationContext)

        setContent {
            GroupeakApp()
        }
    }
}

@Composable
fun GroupeakApp() {
    GroupeakTheme {
        val navController = rememberNavController()

        Surface(color = MaterialTheme.colorScheme.background) {
            RootNavGraph(rootNavController = navController)
        }
    }
}
