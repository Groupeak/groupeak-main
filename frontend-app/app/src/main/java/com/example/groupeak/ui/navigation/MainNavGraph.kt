package com.example.groupeak.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.groupeak.ui.screens.account.AccountScreen
import com.example.groupeak.ui.screens.account.AccountViewModel
import com.example.groupeak.ui.screens.change_email.ChangeEmailScreen
import com.example.groupeak.ui.screens.change_password.ChangePasswordScreen
import com.example.groupeak.ui.screens.feedback.FeedbackScreen
import com.example.groupeak.ui.screens.profile.ProfileScreen
import com.example.groupeak.ui.screens.profile.ProfileViewModel

@Composable
fun MainNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onLogout: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Account.route,
        modifier = modifier
    ) {
        composable(Screen.Account.route) {
            val vm: AccountViewModel = viewModel()
            val state = vm.uiState

            AccountScreen(
                uiState = state,
                onProfileClick = { navController.navigate(Screen.Profile.route) },
                onContactClick = { navController.navigate(Screen.Feedback.route) },
                onChangeEmailClick = { navController.navigate(Screen.ChangeEmail.route) },
                onChangePasswordClick = { navController.navigate(Screen.ChangePassword.route) },
                onLogoutClick = onLogout
            )
        }

        composable(Screen.Profile.route) {
            val vm: ProfileViewModel = viewModel()
            val state = vm.uiState

            ProfileScreen(
                uiState = state,
                onBackClick = { navController.popBackStack() },
                onEditClick = vm::onEditClick,
                onSaveClick = vm::onSaveClick,
                onNotificationsClick = { /* TODO */ },
                onPositionClick = { /* TODO */ },
                onFullNameChange = vm::onFullNameChange,
                onBirthDateChange = vm::onBirthDateChange,
                onPositionChange = vm::onPositionChange,
                onAboutChange = vm::onAboutChange
            )
        }

        composable(Screen.Feedback.route) {
            FeedbackScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.ChangeEmail.route) {
            ChangeEmailScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.ChangePassword.route) {
            ChangePasswordScreen(onBack = { navController.popBackStack() })
        }
    }
}
