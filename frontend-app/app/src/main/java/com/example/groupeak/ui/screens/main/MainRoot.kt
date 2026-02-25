package com.example.groupeak.ui.screens.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.groupeak.ui.components.AppBottomBar
import com.example.groupeak.ui.components.CreateBottomSheetContent
import com.example.groupeak.ui.navigation.MainNavGraph
import com.example.groupeak.ui.navigation.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainRoot(
    onLogout: () -> Unit
) {
    val mainNavController = rememberNavController()

    val backStackEntry by mainNavController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val selectedRoute = when (currentRoute) {
        Screen.ChangeEmail.route,
        Screen.ChangePassword.route -> Screen.Account.route
        else -> currentRoute
    }

    var isCreateSheetVisible by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    fun dismissCreateSheet() {
        scope.launch { sheetState.hide() }
            .invokeOnCompletion { isCreateSheetVisible = false }
    }

    Box {
        Scaffold(
            contentWindowInsets = WindowInsets(0),
            bottomBar = {
                AppBottomBar(
                    navController = mainNavController,
                    selectedRoute = selectedRoute,
                    onAddClick = { isCreateSheetVisible = true }
                )
            }
        ) { innerPadding ->
            MainNavGraph(
                navController = mainNavController,
                modifier = Modifier.padding(innerPadding),
                onLogout = onLogout
            )
        }

        if (isCreateSheetVisible) {
            LaunchedEffect(Unit) { sheetState.show() }

            ModalBottomSheet(
                onDismissRequest = ::dismissCreateSheet,
                sheetState = sheetState,
                containerColor = Color.White,
                tonalElevation = 0.dp,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                dragHandle = null
            ) {
                CreateBottomSheetContent(
                    onCreateProjectClick = {
                        dismissCreateSheet()
                        mainNavController.navigate(Screen.CreateProject.route)
                    },
                    onCreateTaskClick = {
                        dismissCreateSheet()
                        mainNavController.navigate(Screen.CreateTask.route)
                    }
                )
            }
        }
    }
}