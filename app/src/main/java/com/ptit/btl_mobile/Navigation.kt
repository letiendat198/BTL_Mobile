package com.ptit.btl_mobile

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ptit.btl_mobile.ui.screens.home.HomeScreen
import com.ptit.btl_mobile.ui.screens.player.PlayerScreen
import com.ptit.btl_mobile.ui.screens.playlist.PlaylistScreen
import kotlinx.serialization.Serializable

sealed class Destinations {
    @Serializable object HomeScreen
    @Serializable object PlaylistScreen
    @Serializable object PlayerScreen
}

@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier) {
    NavHost(navController = navController, modifier = modifier, startDestination = Destinations.HomeScreen) {
        composable<Destinations.HomeScreen> { HomeScreen() }
        composable<Destinations.PlaylistScreen> { PlaylistScreen() }
        composable<Destinations.PlayerScreen> { PlayerScreen() }
    }
}