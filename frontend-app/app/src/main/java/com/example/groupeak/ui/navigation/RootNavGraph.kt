package com.example.groupeak.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.groupeak.service_locator.ServiceLocator
import com.example.groupeak.ui.screens.login.LoginScreen
import com.example.groupeak.ui.screens.login.LoginViewModel
import com.example.groupeak.ui.screens.main.MainRoot
import com.example.groupeak.ui.screens.register.RegisterScreen
import com.example.groupeak.ui.screens.register.RegisterViewModel
import com.example.groupeak.ui.screens.start.StartScreen

class RegisterViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RegisterViewModel(ServiceLocator.authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
    }
}

class LoginViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(ServiceLocator.authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
    }
}

@Composable
fun RootNavGraph(rootNavController: NavHostController) {
    NavHost(
        navController = rootNavController,
        startDestination = Graph.AUTH
    ) {
        authGraph(rootNavController)

        composable(Graph.MAIN) {
            MainRoot(
                onLogout = {
                    ServiceLocator.sessionManager.clear()
                    rootNavController.navigate(Graph.AUTH) {
                        popUpTo(Graph.MAIN) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

private fun NavGraphBuilder.authGraph(rootNavController: NavHostController) {
    navigation(
        startDestination = Screen.Start.route,
        route = Graph.AUTH
    ) {
        composable(Screen.Start.route) {
            StartScreen(
                onLoginClick = { rootNavController.navigate(Screen.Login.route) },
                onRegisterClick = { rootNavController.navigate(Screen.Register.route) }
            )
        }

        composable(Screen.Login.route) {
            val loginViewModel: LoginViewModel = viewModel(factory = LoginViewModelFactory())
            val state = loginViewModel.uiState

            LoginScreen(
                state = state,
                onBack = { rootNavController.popBackStack() },
                onForgotPassword = {},
                onEmailChange = loginViewModel::onEmailChange,
                onPasswordChange = loginViewModel::onPasswordChange,
                onLoginClick = {
                    loginViewModel.onLoginClick {
                        rootNavController.navigate(Graph.MAIN) {
                            popUpTo(Graph.AUTH) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            val registerViewModel: RegisterViewModel =
                viewModel(factory = RegisterViewModelFactory())
            val state = registerViewModel.uiState

            RegisterScreen(
                state = state,
                onBack = { rootNavController.popBackStack() },
                onForgotPassword = { rootNavController.popBackStack() },
                onFioChange = registerViewModel::onFioChange,
                onEmailChange = registerViewModel::onEmailChange,
                onPasswordChange = registerViewModel::onPasswordChange,
                onSecondPasswordChange = registerViewModel::onSecondPasswordChange,
                onRegisterClick = {
                    registerViewModel.onRegisterClick {
                        rootNavController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    }
                }
            )
        }
    }
}
