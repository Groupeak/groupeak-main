package com.example.groupeak.ui.navigation

// Все экраны и их route в одном месте
sealed class Screen(val route: String) {
    object Start : Screen("start")
    object Login : Screen("login")
    object Register : Screen("register")
    object Account : Screen("account")
    object Profile : Screen("profile")
    object Feedback : Screen("feedback")
    object ChangeEmail : Screen("change_email")
    object ChangePassword : Screen("change_password")

    object Notifications : Screen("notifications")
    object Tasks : Screen("tasks")
    object Calendar : Screen("calendar")
    object CreateProject : Screen("create_project")
    object CreateTask : Screen("create_task")

}
