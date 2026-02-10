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
import com.example.groupeak.ui.screens.change_email.ChangeEmailUiState
import com.example.groupeak.ui.screens.change_email.ChangeEmailViewModel
import com.example.groupeak.ui.screens.change_password.ChangePasswordScreen
import com.example.groupeak.ui.screens.change_password.ChangePasswordUiState
import com.example.groupeak.ui.screens.change_password.ChangePasswordViewModel
import com.example.groupeak.ui.screens.feedback.FeedbackScreen
import com.example.groupeak.ui.screens.profile.ProfileScreen
import com.example.groupeak.ui.screens.profile.ProfileViewModel

// временно для заглушек
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding


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
                onCancelEdit = vm::onCancelEdit,
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
            val viewModel: ChangeEmailViewModel = viewModel()
            val uiState = viewModel.uiState

            ChangeEmailScreen(
                uiState = uiState,
                onBackClick = { navController.popBackStack() },
                onNewEmailChange = viewModel::onNewEmailChange,
                onPasswordChange = viewModel::onPasswordChange,
                onChangeClick = { /* ничего не делаем */ },
                onTogglePasswordVisibility = viewModel::togglePasswordVisibility,
                onNotificationsClick = { /* TODO */ }
            )
        }

        composable(Screen.ChangePassword.route) {
            val viewModel: ChangePasswordViewModel = viewModel()
            val uiState = viewModel.uiState

            ChangePasswordScreen(
                uiState = uiState,
                onBackClick = { navController.popBackStack() },
                onOldPasswordChange = viewModel::onOldPasswordChange,
                onNewPasswordChange = viewModel::onNewPasswordChange,
                onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
                onChangeClick = { /* ничего не делаем */ },
                onToggleOldPasswordVisibility = viewModel::toggleOldPasswordVisibility,
                onToggleNewPasswordVisibility = viewModel::toggleNewPasswordVisibility,
                onToggleConfirmPasswordVisibility = viewModel::toggleConfirmPasswordVisibility,
                onNotificationsClick = { /* TODO */ }
            )
        }

        composable(Screen.Tasks.route) {
            Text("Задачи — заглушка", modifier = Modifier.padding(16.dp))
        }

        composable(Screen.Calendar.route) {
            Text("Календарь — заглушка", modifier = Modifier.padding(16.dp))
        }
    }
}
