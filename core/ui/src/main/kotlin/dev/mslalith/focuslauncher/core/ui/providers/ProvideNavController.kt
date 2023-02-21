package dev.mslalith.focuslauncher.core.ui.providers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

val LocalNavController = staticCompositionLocalOf<NavHostController> {
    error("No NavHostController provided")
}

@Composable
fun ProvideNavController(
    content: @Composable () -> Unit
) {
    val navController = rememberNavController()
    CompositionLocalProvider(LocalNavController provides navController) {
        content()
    }
}
