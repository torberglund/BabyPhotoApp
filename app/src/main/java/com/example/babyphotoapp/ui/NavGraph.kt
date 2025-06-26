// File: app/src/main/java/com/example/babyphotoapp/ui/NavGraph.kt
package com.example.babyphotoapp.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "camera") {
        composable("camera") {
            CameraScreen(navController)
        }
        composable("review") {
            ReviewScreen(navController)
        }

        composable("settings") { SettingsScreen(navController)}


    }
}
