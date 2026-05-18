package com.example.groupeak.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import com.example.groupeak.ui.screens.project.ProjectScreen
import com.example.groupeak.ui.screens.project.ProjectTab
import com.example.groupeak.ui.screens.project.mockProjectUiState
import com.example.groupeak.ui.screens.project_for_owner.ProjectForOwnerScreen
import com.example.groupeak.ui.screens.project_for_owner.ProjectOwnerTab
import com.example.groupeak.ui.screens.project_for_owner.mockProjectForOwnerUiState

import com.example.groupeak.ui.screens.tasks.TasksScreen
import com.example.groupeak.ui.screens.tasks.TasksViewModel
import com.example.groupeak.ui.screens.tasks.TasksViewModelFactory
import com.example.groupeak.ui.screens.events.EventsScreen
import com.example.groupeak.ui.screens.events.EventsViewModel

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

            val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                    if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
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
                onAllTasksClick = { navController.navigate(Screen.Tasks.createRoute()) },
                onTaskClick = { projectId, taskId ->
                    navController.navigate(Screen.Tasks.createRoute(projectId, taskId))
                },
                onProjectClick = { projectId, isOwner ->
                    navController.navigate(Screen.ProjectTasks.createRoute(projectId, isOwner))
                },
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

        composable(
            route = Screen.Tasks.route,
            arguments = listOf(
                navArgument("openProjectId") {
                    type = NavType.LongType
                    defaultValue = -1L
                },
                navArgument("openTaskId") {
                    type = NavType.LongType
                    defaultValue = -1L
                },
            ),
        ) { backStackEntry ->
            val openProjectId = backStackEntry.arguments?.getLong("openProjectId")?.takeIf { it > 0 }
            val openTaskId = backStackEntry.arguments?.getLong("openTaskId")?.takeIf { it > 0 }

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

            // Auto-open task details if navigated with specific task
            LaunchedEffect(openProjectId, openTaskId) {
                if (openProjectId != null && openTaskId != null) {
                    vm.openTask(openProjectId, openTaskId)
                }
            }

            val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                    if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                        vm.refreshTasks(clearStatus = false)
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
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
                onConfirmDelete = vm::confirmDelete,
                onCancelDelete = vm::cancelDelete,
                onDeleteTask = vm::deleteTask,
            )
        }


        composable(
            route = Screen.ProjectTasks.route,
            arguments = listOf(
                navArgument("projectId") { type = NavType.LongType },
                navArgument("isOwner") {
                    type = NavType.BoolType
                    defaultValue = false
                },
            ),
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getLong("projectId")
                ?: return@composable

            val isOwner = backStackEntry.arguments?.getBoolean("isOwner") ?: false
            android.util.Log.e("INVITE_DEBUG", "=== ProjectTasks: projectId=$projectId, isOwner=$isOwner ===")

            if (isOwner) {
                val vm: com.example.groupeak.ui.screens.project_for_owner.ProjectForOwnerViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            if (modelClass.isAssignableFrom(com.example.groupeak.ui.screens.project_for_owner.ProjectForOwnerViewModel::class.java)) {
                                @Suppress("UNCHECKED_CAST")
                                return com.example.groupeak.ui.screens.project_for_owner.ProjectForOwnerViewModel(
                                    projectId,
                                    ServiceLocator.projectRepository,
                                    ServiceLocator.taskRepository,
                                    ServiceLocator.eventRepository
                                ) as T
                            }
                            throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
                        }
                    }
                )

                var selectedTab by rememberSaveable(projectId) {
                    mutableStateOf(ProjectOwnerTab.TASKS)
                }

                val uiState = vm.uiState.copy(selectedTab = selectedTab)

                val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
                DisposableEffect(lifecycleOwner) {
                    val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                        if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                            vm.refreshData()
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                ProjectForOwnerScreen(
                    uiState = uiState,
                    onBack = { navController.popBackStack() },
                    onNotificationsClick = { navController.navigate(Screen.Notifications.route) },
                    onTabSelected = { selectedTab = it },
                    onInfoClick = { },
                    onFilterClick = { },
                    onTaskClick = { },
                    onMemberClick = { },
                    onInviteMemberClick = { },
                    onDeleteTask = { vm.deleteTask(it) },
                    onDeleteEvent = { vm.deleteEvent(it) },
                    onUpdateEvent = { id, title, date, time, link, desc, parts ->
                        vm.updateEvent(id, title, date, time, link, desc, parts)
                    },
                    onDeleteMember = { },
                    onUpdateTaskDate = { id, date -> vm.approveTask(id, date) },
                    onApproveTask = { id, date -> vm.approveTask(id, date) },
                    onRejectTask = { vm.rejectTask(it) },
                    onInviteEmailChange = { vm.updateInviteEmail(it) },
                    onInvitePositionChange = { vm.updateInvitePosition(it) },
                    onSendInvite = { vm.sendInvite() },
                    onSendInviteWithEmail = { email, _ -> vm.sendInviteDirectly(email) },
                    onNewDateChange = { },
                )
            } else {
                val vm: com.example.groupeak.ui.screens.project.ProjectViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            if (modelClass.isAssignableFrom(com.example.groupeak.ui.screens.project.ProjectViewModel::class.java)) {
                                @Suppress("UNCHECKED_CAST")
                                return com.example.groupeak.ui.screens.project.ProjectViewModel(
                                    projectId,
                                    ServiceLocator.projectRepository,
                                    ServiceLocator.taskRepository
                                ) as T
                            }
                            throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
                        }
                    }
                )

                var selectedTab by rememberSaveable(projectId) {
                    mutableStateOf(ProjectTab.TASKS)
                }

                val uiState = vm.uiState.copy(selectedTab = selectedTab)

                val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
                DisposableEffect(lifecycleOwner) {
                    val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                        if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                            vm.refreshData()
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                ProjectScreen(
                    uiState = uiState,
                    onBack = { navController.popBackStack() },
                    onNotificationsClick = { navController.navigate(Screen.Notifications.route) },
                    onTabSelected = { selectedTab = it },
                    onInfoClick = { },
                    onFilterClick = { },
                    onTaskClick = { },
                    onMemberClick = { },
                    onInviteMemberClick = { },
                )
            }
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
                                    previousRoute?.startsWith(Screen.Tasks.baseRoute) == true ||
                                    previousRoute == Screen.ProjectTasks.route ||
                                    previousRoute?.startsWith("$projectTasksRouteBase/") == true

                        if (cameFromTasks) {
                            previousEntry?.savedStateHandle?.set("task_created", true)
                            navController.popBackStack()
                        } else {
                            navController.navigate(Screen.Tasks.createRoute()) {
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
                            return CreateEventViewModel(
                                ServiceLocator.projectRepository,
                                ServiceLocator.eventRepository,
                            ) as T
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

        // Экран мероприятий
        composable(Screen.Events.route) {
            val vm: EventsViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        if (modelClass.isAssignableFrom(EventsViewModel::class.java)) {
                            @Suppress("UNCHECKED_CAST")
                            return EventsViewModel(
                                ServiceLocator.eventRepository,
                                ServiceLocator.projectRepository,
                            ) as T
                        }
                        throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
                    }
                }
            )
            val uiState by vm.uiState.collectAsState()

            val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
            androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
                val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                    if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                        vm.refreshEvents()
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            EventsScreen(
                uiState = uiState,
                onBackClick = { navController.popBackStack() },
                onEventClick = { event -> vm.selectEvent(event) },
                onNotificationsClick = { navController.navigate(Screen.Notifications.route) },
                onDismissDialog = { vm.clearSelection() }
            )
        }
    }
}