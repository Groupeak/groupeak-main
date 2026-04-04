package com.example.groupeak.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.groupeak.service_locator.ServiceLocator
import com.example.groupeak.ui.screens.account.AccountScreen
import com.example.groupeak.ui.screens.account.AccountViewModel
import com.example.groupeak.ui.screens.change_email.ChangeEmailScreen
import com.example.groupeak.ui.screens.change_email.ChangeEmailViewModel
import com.example.groupeak.ui.screens.change_password.ChangePasswordScreen
import com.example.groupeak.ui.screens.change_password.ChangePasswordViewModel
import com.example.groupeak.ui.screens.create_event.CreateEventScreen
import com.example.groupeak.ui.screens.create_event.CreateEventViewModel
import com.example.groupeak.ui.screens.create_project.CreateProjectScreen
import com.example.groupeak.ui.screens.create_project.CreateProjectViewModel
import com.example.groupeak.ui.screens.create_task.CreateTaskScreen
import com.example.groupeak.ui.screens.create_task.CreateTaskViewModel
import com.example.groupeak.ui.screens.feedback.FeedbackScreen
import com.example.groupeak.ui.screens.home.HomeScreen
import com.example.groupeak.ui.screens.home.HomeViewModel
import com.example.groupeak.ui.screens.notifications.NotificationsScreen
import com.example.groupeak.ui.screens.profile.ProfileScreen
import com.example.groupeak.ui.screens.profile.ProfileViewModel
import com.example.groupeak.ui.screens.tasks.TasksScreen
import com.example.groupeak.ui.screens.tasks.TasksViewModel
import com.example.groupeak.ui.screens.tasks.TasksViewModelFactory

@Composable
fun MainNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onLogout: () -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier,
    ) {
        composable(Screen.CreateProject.route) {
            val vm: CreateProjectViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        if (modelClass.isAssignableFrom(CreateProjectViewModel::class.java)) {
                            @Suppress("UNCHECKED_CAST")
                            return CreateProjectViewModel(
                                ServiceLocator.projectRepository,
                            ) as T
                        }
                        throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
                    }
                }
            )

            val uiState = vm.uiState

            CreateProjectScreen(
                uiState = uiState,
                onBack = { navController.popBackStack() },
                onProjectNameChange = vm::onProjectNameChange,
                onDeadlineChange = vm::onDeadlineChange,
                onDescriptionChange = vm::onDescriptionChange,
                onCreateClick = {
                    vm.onCreateClick {
                        navController.popBackStack()
                    }
                },
                hasNotifications = true,
                onNotificationsClick = { navController.navigate(Screen.Notifications.route) },
            )
        }

        composable(Screen.Home.route) {
            val vm: HomeViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                            @Suppress("UNCHECKED_CAST")
                            return HomeViewModel(
                                ServiceLocator.userRepository,
                                ServiceLocator.projectRepository,
                                ServiceLocator.taskRepository,
                                ServiceLocator.sessionManager,
                            ) as T
                        }
                        throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
                    }
                }
            )

            val state = vm.uiState
            val lifecycleOwner = LocalLifecycleOwner.current

            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        vm.refreshHomeContent()
                    }
                }

                lifecycleOwner.lifecycle.addObserver(observer)

                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            HomeScreen(
                uiState = state,
                onProfileClick = { navController.navigate(Screen.Profile.route) },
                onNotificationsClick = { navController.navigate(Screen.Notifications.route) },
                onAllTasksClick = { navController.navigate(Screen.Tasks.route) },
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
                onLogoutClick = onLogout,
                onNotificationsClick = { navController.navigate(Screen.Notifications.route) },
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
                onPositionClick = { },
                onFullNameChange = vm::onFullNameChange,
                onBirthDateChange = vm::onBirthDateChange,
                onPositionChange = vm::onPositionChange,
                onAboutChange = vm::onAboutChange,
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
                onNotificationsClick = { navController.navigate(Screen.Notifications.route) },
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
                onNotificationsClick = { navController.navigate(Screen.Notifications.route) },
            )
        }

        composable(Screen.Tasks.route) { backStackEntry ->
            val vm: TasksViewModel = viewModel(
                factory = TasksViewModelFactory(
                    taskRepository = ServiceLocator.taskRepository,
                    projectRepository = ServiceLocator.projectRepository,
                )
            )

            val uiState = vm.uiState
            val taskCreated by backStackEntry
                .savedStateHandle
                .getStateFlow("task_created", false)
                .collectAsState()

            LaunchedEffect(taskCreated) {
                if (taskCreated) {
                    vm.refreshTasks(clearStatus = true)
                    backStackEntry.savedStateHandle["task_created"] = false
                }
            }

            TasksScreen(
                uiState = uiState,
                onBack = { navController.popBackStack() },
                onNotificationsClick = { navController.navigate(Screen.Notifications.route) },
                onTabSelected = vm::onTabSelected,
                onApplyFilters = vm::applyFilters,
                onResetFilters = vm::resetFilters,
                onTaskClick = vm::openTask,
                onDismissTaskDetails = vm::closeTaskDetails,
                onSubmitTask = vm::submitTaskForReview,
            )
        }

        composable(
            route = Screen.ProjectTasks.route,
            arguments = listOf(
                navArgument("projectId") { type = NavType.LongType }
            ),
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getLong("projectId")
                ?: return@composable

            val vm: TasksViewModel = viewModel(
                factory = TasksViewModelFactory(
                    taskRepository = ServiceLocator.taskRepository,
                    projectRepository = ServiceLocator.projectRepository,
                    initialProjectId = projectId,
                )
            )

            val uiState = vm.uiState
            val taskCreated by backStackEntry
                .savedStateHandle
                .getStateFlow("task_created", false)
                .collectAsState()

            LaunchedEffect(taskCreated) {
                if (taskCreated) {
                    vm.refreshTasks(clearStatus = true)
                    backStackEntry.savedStateHandle["task_created"] = false
                }
            }

            TasksScreen(
                uiState = uiState,
                onBack = { navController.popBackStack() },
                onNotificationsClick = { navController.navigate(Screen.Notifications.route) },
                onTabSelected = vm::onTabSelected,
                onApplyFilters = vm::applyFilters,
                onResetFilters = vm::resetFilters,
                onTaskClick = vm::openTask,
                onDismissTaskDetails = vm::closeTaskDetails,
                onSubmitTask = vm::submitTaskForReview,
            )
        }

        composable(Screen.CreateTask.route) {
            val vm: CreateTaskViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        if (modelClass.isAssignableFrom(CreateTaskViewModel::class.java)) {
                            @Suppress("UNCHECKED_CAST")
                            return CreateTaskViewModel(
                                ServiceLocator.projectRepository,
                                ServiceLocator.taskRepository,
                            ) as T
                        }
                        throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
                    }
                }
            )

            val uiState = vm.uiState

            CreateTaskScreen(
                uiState = uiState,
                onBackClick = { navController.popBackStack() },
                onTitleChange = vm::onTitleChange,
                onProjectSelected = vm::onProjectSelected,
                onDeadlineChange = vm::onDeadlineChange,
                onToggleParticipant = vm::onToggleParticipant,
                onDescriptionChange = vm::onDescriptionChange,
                onPriorityChange = vm::onPriorityChange,
                onCreateClick = {
                    vm.onCreateClick {
                        val previousEntry = navController.previousBackStackEntry
                        val previousRoute = previousEntry?.destination?.route
                        val projectTasksRouteBase = Screen.ProjectTasks.route.substringBefore("/{")

                        val cameFromTasks =
                            previousRoute == Screen.Tasks.route ||
                                    previousRoute == Screen.ProjectTasks.route ||
                                    previousRoute?.startsWith("$projectTasksRouteBase/") == true

                        if (cameFromTasks) {
                            previousEntry?.savedStateHandle?.set("task_created", true)
                            navController.popBackStack()
                        } else {
                            navController.navigate(Screen.Tasks.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    }
                },
            )
        }

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
                },
            )
        }

        composable(Screen.Notifications.route) {
            NotificationsScreen(
                onBack = { navController.popBackStack() },
            )
        }
    }
}