package com.ptit.btl_mobile

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.ptit.btl_mobile.ui.screens.home.HomeScreen
import com.ptit.btl_mobile.ui.screens.library.LibraryScreen
import com.ptit.btl_mobile.ui.screens.playlist.AddSongsToPlaylistScreen
import com.ptit.btl_mobile.ui.screens.playlist.CreatePlaylistScreen
import com.ptit.btl_mobile.ui.screens.playlist.PlaylistDetailScreen
import com.ptit.btl_mobile.ui.screens.playlist.PlaylistScreen
import com.ptit.btl_mobile.ui.screens.playlist.SelectSongsScreen
import kotlinx.serialization.Serializable

sealed class Destinations {
    @Serializable object HomeScreen
    @Serializable object SelectSongsScreen
    @Serializable object CreatePlaylistScreen
    @Serializable object PlaylistScreen
    @Serializable object PlaylistDetailScreen
    @Serializable data class AddSongsToPlaylist(val playlistId: Long)
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
            PlaylistDetailScreen(
                onBack = { navController.popBackStack() },
                onAddSongs = { playlistId ->
                    navController.navigate(Destinations.AddSongsToPlaylist(playlistId))
                }
            )
        }

        composable<Destinations.AddSongsToPlaylist> { backStackEntry ->
            val args: Destinations.AddSongsToPlaylist = backStackEntry.toRoute()
            AddSongsToPlaylistScreen(
                playlistId = args.playlistId,
                onBack = { navController.popBackStack() }
            )
        }

        composable<Destinations.CreatePlaylistScreen> {
            CreatePlaylistScreen(
                onBack = { navController.popBackStack() },
                onNavToSelectSongs = { navController.navigate(Destinations.SelectSongsScreen) },
            )
        }

        composable<Destinations.SelectSongsScreen> {
            SelectSongsScreen(
                onBack = { navController.navigate(Destinations.PlaylistScreen) }
            )
        }

        composable<Destinations.LibraryScreen> { LibraryScreen() }
    }
}
