package com.example.groupeak.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.groupeak.service_locator.ServiceLocator
import com.example.groupeak.ui.screens.account.AccountScreen
import com.example.groupeak.ui.screens.account.AccountViewModel
import com.example.groupeak.ui.screens.change_email.ChangeEmailScreen
import com.example.groupeak.ui.screens.change_email.ChangeEmailViewModel
import com.example.groupeak.ui.screens.change_password.ChangePasswordScreen
import com.example.groupeak.ui.screens.change_password.ChangePasswordViewModel
import com.example.groupeak.ui.screens.createproject.CreateProjectScreen
import com.example.groupeak.ui.screens.feedback.FeedbackScreen
import com.example.groupeak.ui.screens.notifications.NotificationsScreen
import com.example.groupeak.ui.screens.profile.ProfileScreen
import com.example.groupeak.ui.screens.profile.ProfileViewModel
import com.example.groupeak.ui.screens.tasks.TasksScreen
import com.example.groupeak.ui.screens.create_task.CreateTaskScreen
import com.example.groupeak.ui.screens.create_task.CreateTaskViewModel
import com.example.groupeak.ui.screens.create_event.CreateEventScreen
import com.example.groupeak.ui.screens.create_event.CreateEventViewModel

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
        composable(Screen.CreateProject.route) {
            CreateProjectScreen(
                onBack = { navController.popBackStack() },
                hasNotifications = true,
                onNotificationsClick = { navController.navigate(Screen.Notifications.route) }
            )
        }

        composable(Screen.Account.route) {
            val vm: AccountViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        if (modelClass.isAssignableFrom(AccountViewModel::class.java)) {
                            @Suppress("UNCHECKED_CAST")
                            return AccountViewModel(
                                ServiceLocator.userRepository,
                                ServiceLocator.sessionManager,
                            ) as T
                        }
                        throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
                    }
                }
            )

            val state = vm.uiState

            AccountScreen(
                uiState = state,
                onProfileClick = { navController.navigate(Screen.Profile.route) },
                onContactClick = { navController.navigate(Screen.Feedback.route) },
                onChangeEmailClick = { navController.navigate(Screen.ChangeEmail.route) },
                onChangePasswordClick = { navController.navigate(Screen.ChangePassword.route) },
                onNotificationsClick = { navController.navigate(Screen.Notifications.route) },
                onLogoutClick = onLogout
            )
        }

        composable(Screen.Profile.route) {
            val vm: ProfileViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                            @Suppress("UNCHECKED_CAST")
                            return ProfileViewModel(
                                ServiceLocator.userRepository,
                                ServiceLocator.sessionManager,
                            ) as T
                        }
                        throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
                    }
                }
            )

            val state = vm.uiState

            ProfileScreen(
                uiState = state,
                onBackClick = { navController.popBackStack() },
                onEditClick = vm::onEditClick,
                onSaveClick = vm::onSaveClick,
                onCancelEdit = vm::onCancelEdit,
                onNotificationsClick = { navController.navigate(Screen.Notifications.route) },
                onPositionClick = { /* TODO */ },
                onFullNameChange = vm::onFullNameChange,
                onBirthDateChange = vm::onBirthDateChange,
                onPositionChange = vm::onPositionChange,
                onAboutChange = vm::onAboutChange
            )
        }

        composable(Screen.Feedback.route) {
            val user by ServiceLocator.sessionManager.user.collectAsState()

            FeedbackScreen(
                onBack = { navController.popBackStack() },
                onNotificationsClick = { navController.navigate(Screen.Notifications.route) },
                senderEmail = user?.email.orEmpty(),
            )
        }

        composable(Screen.ChangeEmail.route) {
            val vm: ChangeEmailViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        if (modelClass.isAssignableFrom(ChangeEmailViewModel::class.java)) {
                            @Suppress("UNCHECKED_CAST")
                            return ChangeEmailViewModel(
                                ServiceLocator.userRepository,
                                ServiceLocator.sessionManager,
                            ) as T
                        }
                        throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
                    }
                }
            )

            val uiState = vm.uiState

            ChangeEmailScreen(
                uiState = uiState,
                onBackClick = { navController.popBackStack() },
                onNewEmailChange = vm::onNewEmailChange,
                onPasswordChange = vm::onPasswordChange,
                onChangeClick = vm::onChangeClick,
                onTogglePasswordVisibility = vm::togglePasswordVisibility,
                onNotificationsClick = { navController.navigate(Screen.Notifications.route) }
            )
        }

        composable(Screen.ChangePassword.route) {
            val vm: ChangePasswordViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        if (modelClass.isAssignableFrom(ChangePasswordViewModel::class.java)) {
                            @Suppress("UNCHECKED_CAST")
                            return ChangePasswordViewModel(
                                ServiceLocator.userRepository,
                            ) as T
                        }
                        throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
                    }
                }
            )

            val uiState = vm.uiState

            ChangePasswordScreen(
                uiState = uiState,
                onBackClick = { navController.popBackStack() },
                onOldPasswordChange = vm::onOldPasswordChange,
                onNewPasswordChange = vm::onNewPasswordChange,
                onConfirmPasswordChange = vm::onConfirmPasswordChange,
                onChangeClick = vm::onChangeClick,
                onToggleOldPasswordVisibility = vm::toggleOldPasswordVisibility,
                onToggleNewPasswordVisibility = vm::toggleNewPasswordVisibility,
                onToggleConfirmPasswordVisibility = vm::toggleConfirmPasswordVisibility,
                onNotificationsClick = {
                    navController.navigate(Screen.Notifications.route)
                }
            )
        }

        composable(Screen.Tasks.route) {
            TasksScreen(
                onBack = { navController.popBackStack() },
                onNotificationsClick = { navController.navigate(Screen.Notifications.route) }
            )
        }

        // Добавить задачу
        composable(Screen.CreateTask.route) {
            val vm: CreateTaskViewModel = viewModel()
            val uiState = vm.uiState

            CreateTaskScreen(
                uiState = uiState,
                onBackClick = { navController.popBackStack() },
                onTitleChange = vm::onTitleChange,
                onProjectChange = vm::onProjectChange,
                onDeadlineChange = vm::onDeadlineChange,
                onParticipantsChange = vm::onParticipantsChange,
                onDescriptionChange = vm::onDescriptionChange,
                onPriorityChange = vm::onPriorityChange,
                onCreateClick = {
                    vm.onCreateClick {
                        // Успех: просто вернуться
                        navController.popBackStack()
                    }
                }
            )
        }

        // Создание мероприятия
        composable(Screen.CreateEvent.route) {
            val vm: CreateEventViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        if (modelClass.isAssignableFrom(CreateEventViewModel::class.java)) {
                            @Suppress("UNCHECKED_CAST")
                            return CreateEventViewModel() as T
                        }
                        throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
                    }
                }
            )
            val uiState by vm.uiState.collectAsState()

            CreateEventScreen(
                uiState = uiState,
                onBackClick = { navController.popBackStack() },
                onTitleChange = vm::onTitleChange,
                onProjectChange = vm::onProjectChange,
                onDateChange = vm::onDateChange,
                onTimeChange = vm::onTimeChange,
                onParticipantsChange = vm::onParticipantsChange,
                onLinkChange = vm::onLinkChange,
                onDescriptionChange = vm::onDescriptionChange,
                onCreateClick = {
                    vm.onCreateClick {
                        navController.popBackStack()
                    }
                }
            )
        }

        // Уведомления
        composable(Screen.Notifications.route) {
            NotificationsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
