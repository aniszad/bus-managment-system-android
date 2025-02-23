package com.azcode.busmanagmentsystem.presentation.navigation


sealed class NavRoutes(val route: String) {
    object Login : NavRoutes("login")
    object Register : NavRoutes("register")
    object Home : NavRoutes("home")
    object Details : NavRoutes("details/{itemId}") // Example with argument

    fun withArgs(vararg args: String): String {
        return route.replace("{itemId}", args.joinToString("/"))
    }
}
