package com.ptit.btl_mobile

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.ptit.btl_mobile.ui.screens.home.HomeScreen
import com.ptit.btl_mobile.ui.screens.library.LibraryScreen
import com.ptit.btl_mobile.ui.screens.player.PlayerScreen
import com.ptit.btl_mobile.ui.screens.playlist.CreatePlaylistScreen
import com.ptit.btl_mobile.ui.screens.playlist.PlaylistDetailScreen
import com.ptit.btl_mobile.ui.screens.playlist.PlaylistScreen
import com.ptit.btl_mobile.ui.screens.playlist.SelectSongsScreen
import kotlinx.serialization.Serializable

// HOW TO ADD A NEW DESTINATION:
// 1. Add an object or data class in Destinations
// 2. Add a composable() with type of added destinations and a lambda calling the composable
// See: https://developer.android.com/guide/navigation/design
//
// Note: DO NOT pass NavHostController to a child composable, pass a callback to navigate instead
// See: https://developer.android.com/guide/navigation/use-graph/navigate

sealed class Destinations {
    @Serializable object HomeScreen
    @Serializable object SelectSongsScreen
    @Serializable object CreatePlaylistScreen
    @Serializable object PlaylistScreen
    @Serializable object PlaylistDetailScreen
    @Serializable object LibraryScreen
}

@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        modifier = modifier,
        startDestination = Destinations.HomeScreen,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None }
    ) {
        composable<Destinations.HomeScreen> { HomeScreen() }
        composable<Destinations.PlaylistScreen> {
            PlaylistScreen(
                onNavToCreatePlaylist = {
                    navController.navigate(Destinations.CreatePlaylistScreen)
                },
                onNavToPlaylistDetailsScreen = {
                    navController.navigate(Destinations.PlaylistDetailScreen)
                }
            )
        }

        composable<Destinations.PlaylistDetailScreen> {
            PlaylistDetailScreen(onBack = { navController.popBackStack() })
        }

        composable<Destinations.CreatePlaylistScreen> {
            CreatePlaylistScreen(
                onBack = { navController.popBackStack() },
                onNavToSelectSongs = { navController.navigate(Destinations.SelectSongsScreen) },
            )
        }
        composable<Destinations.SelectSongsScreen> { SelectSongsScreen(
            onBack = {navController.navigate(
                Destinations.PlaylistScreen
            )}
        ) }
        composable<Destinations.LibraryScreen> { LibraryScreen() }
    }
}