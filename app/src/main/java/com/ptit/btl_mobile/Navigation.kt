package com.ptit.btl_mobile

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.ptit.btl_mobile.ui.components.TopAppBarContent
import com.ptit.btl_mobile.ui.screens.home.HomeScreen
import com.ptit.btl_mobile.ui.screens.library.LibraryScreen
import com.ptit.btl_mobile.ui.screens.playlist.AddSongsToPlaylistScreen
import com.ptit.btl_mobile.ui.screens.home.autoGeneratePlaylist.AutoGeneratePlaylistScreen
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
    @Serializable object AutoGeneratePlaylistScreen
    @Serializable data class AddSongsToPlaylist(val playlistId: Long)
    @Serializable object LibraryScreen
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onSetTopAppBar: (TopAppBarContent) -> Unit
) {
    NavHost(
        navController = navController,
        modifier = modifier,
        startDestination = Destinations.HomeScreen,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None }
    ) {
        composable<Destinations.HomeScreen> {
            HomeScreen(
                onNavigateToAutoPlaylist = {
                    navController.navigate(Destinations.AutoGeneratePlaylistScreen)
                },
                onNavigateToPlaylistDetail = { _ ->
                    navController.navigate(Destinations.PlaylistDetailScreen)
                }
            )
        }

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

        // ← THÊM MỚI: Auto Generate Playlist Screen
        composable<Destinations.AutoGeneratePlaylistScreen> {
            AutoGeneratePlaylistScreen(
                onBack = { navController.popBackStack() },
                onPlaylistGenerated = {
                    navController.popBackStack()
                }
            )
        }

        composable<Destinations.LibraryScreen> {
            LibraryScreen(onSetTopAppBar = onSetTopAppBar)
        }
    }
}