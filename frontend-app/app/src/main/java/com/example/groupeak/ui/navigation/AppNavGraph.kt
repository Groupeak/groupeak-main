package com.example.groupeak.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.groupeak.service_locator.ServiceLocator
import com.example.groupeak.ui.screens.login.LoginScreen
import com.example.groupeak.ui.screens.login.LoginViewModel
import com.example.groupeak.ui.screens.register.RegisterScreen
import com.example.groupeak.ui.screens.register.RegisterViewModel
import com.example.groupeak.ui.screens.start.StartScreen

// Фабрика для RegisterViewModel
class RegisterViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RegisterViewModel(ServiceLocator.authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
    }
}

// Фабрика для LoginViewModel
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
fun AppNavGraph(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = Screen.Start.route
    ) {
        composable(Screen.Start.route) {
            StartScreen(
                onLoginClick = { navController.navigate(Screen.Login.route) },
                onRegisterClick = { navController.navigate(Screen.Register.route) }
            )
        }

        composable(Screen.Login.route) {
            val loginViewModel: LoginViewModel = viewModel(
                factory = LoginViewModelFactory()
            )
            val state = loginViewModel.uiState

            LoginScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onForgotPassword = {},
                onEmailChange = loginViewModel::onEmailChange,
                onPasswordChange = loginViewModel::onPasswordChange,
                onLoginClick = {
                    loginViewModel.onLoginClick {
                        navController.navigate(Screen.Start.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            val registerViewModel: RegisterViewModel = viewModel(
                factory = RegisterViewModelFactory()
            )
            val state = registerViewModel.uiState

            RegisterScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onForgotPassword = {
                    navController.popBackStack()
                },
                onFioChange = registerViewModel::onFioChange,
                onEmailChange = registerViewModel::onEmailChange,
                onPasswordChange = registerViewModel::onPasswordChange,
                onSecondPasswordChange = registerViewModel::onSecondPasswordChange,
                onRegisterClick = {
                    registerViewModel.onRegisterClick {
                        // успешная регистрация → на Login
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    }
                }
            )
        }
    }
}
