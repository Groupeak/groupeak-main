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
import com.example.groupeak.ui.screens.notifications.NotificationsScreen
import com.example.groupeak.ui.screens.profile.ProfileScreen
import com.example.groupeak.ui.screens.profile.ProfileViewModel
import com.example.groupeak.ui.navigation.Screen.*

@Composable
fun MainNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onLogout: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Account.route,
        modifier = modifier
    ) {
        composable(Account.route) {
            val vm: AccountViewModel = viewModel()
            val state = vm.uiState

            AccountScreen(
                uiState = state,
                onProfileClick = { navController.navigate(Profile.route) },
                onContactClick = { navController.navigate(Feedback.route) },
                onChangeEmailClick = { navController.navigate(ChangeEmail.route) },
                onChangePasswordClick = { navController.navigate(ChangePassword.route) },
                onNotificationsClick = { navController.navigate(Notifications.route) },
                onLogoutClick = onLogout
            )
        }

        composable(Profile.route) {
            val vm: ProfileViewModel = viewModel()
            val state = vm.uiState

            ProfileScreen(
                uiState = state,
                onBackClick = { navController.popBackStack() },
                onEditClick = vm::onEditClick,
                onSaveClick = vm::onSaveClick,
                onCancelEdit = vm::onCancelEdit,
                onNotificationsClick = { navController.navigate(Notifications.route) },
                onPositionClick = { /* TODO */ },
                onFullNameChange = vm::onFullNameChange,
                onBirthDateChange = vm::onBirthDateChange,
                onPositionChange = vm::onPositionChange,
                onAboutChange = vm::onAboutChange
            )
        }

        composable(Feedback.route) {
            FeedbackScreen(
                onBack = { navController.popBackStack() },
                onNotificationsClick = { navController.navigate(Notifications.route) }
            )
        }

        composable(ChangeEmail.route) {
            ChangeEmailScreen(onBack = { navController.popBackStack() })
        }

        composable(ChangePassword.route) {
            ChangePasswordScreen(onBack = { navController.popBackStack() })
        }

        composable(Notifications.route) {
            NotificationsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
