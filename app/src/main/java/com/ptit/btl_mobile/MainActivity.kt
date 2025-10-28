package com.ptit.btl_mobile

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresExtension
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ptit.btl_mobile.model.database.Artist
import com.ptit.btl_mobile.model.database.Database
import com.ptit.btl_mobile.model.database.Song
import com.ptit.btl_mobile.model.database.SongArtistCrossRef
import com.ptit.btl_mobile.model.media.MediaLoader
import com.ptit.btl_mobile.model.media.PlaybackService
import com.ptit.btl_mobile.ui.components.FloatingPlayer
import com.ptit.btl_mobile.ui.screens.player.PlayerScreen
import com.ptit.btl_mobile.ui.screens.player.PlayerViewModel
import com.ptit.btl_mobile.ui.theme.BTL_MobileTheme
import com.ptit.btl_mobile.util.DateConverter
import com.ptit.btl_mobile.util.isRoute
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Date

// Init datastore
val Context.dataStore: DataStore<Preferences> by preferencesDataStore("settings")

class MainActivity : ComponentActivity() {
    @RequiresExtension(extension = Build.VERSION_CODES.R, version = 1)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Init database
        Database(this.applicationContext)

        // Request permission
        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    Log.d("PERMISSION", "Permission granted")
                    MediaLoader(this, this.lifecycleScope).updateOrReloadMedia()
                }
                else {
                    Log.d("PERMISSION", "Permission denied")
                }
        }

        requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)

        setContent {
            BTL_MobileTheme {
                    AppNavLayout()
            }
        }
    }

    override fun onStart() {
        super.onStart()

        val playerViewModel = viewModels<PlayerViewModel>()

        val mediaSessionToken = SessionToken(this,
            ComponentName(this, PlaybackService::class.java))
        val controllerFeature = MediaController.Builder(this, mediaSessionToken).buildAsync()
        controllerFeature.addListener({
            playerViewModel.value.mediaController = controllerFeature.get()
        }, ContextCompat.getMainExecutor(this))
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavLayout() {
    val navController = rememberNavController()
    var showPlayer by remember { mutableStateOf(false) }

    SharedTransitionLayout {
        // Don't put AnimatedContent at root. Otherwise, when changing state, there won't be any
        // composable shown and window background will show through, causing white flickering
        Scaffold(
            bottomBar = {
                if (!showPlayer) {
                    BottomNavBar(navController)
                }
            }
        ) { innerPadding ->
            AnimatedContent(
                showPlayer,
            ) { shouldShowPlayer ->
                if (!shouldShowPlayer) {
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(10.dp)
                    ) {
                        // Like Outlet in React Router
                        // Every composable within this NavHost will show up in this scaffold body
                        AppNavHost(
                            navController,
                            modifier = Modifier.weight(1f)
                        )
                        FloatingPlayer(
                            this@SharedTransitionLayout,
                            this@AnimatedContent,
                            onShowPlayer = {
                                showPlayer = true
                            }
                        )
                    }
                }
                else {
                    PlayerScreen(
                        this@SharedTransitionLayout,
                        this@AnimatedContent,
                        onBack = {
                            showPlayer = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
        NavigationBarItem(
            selected = currentDestination?.isRoute(Destinations.HomeScreen::class) == true,
            onClick = {
                navController.navigate(Destinations.HomeScreen)
            },
            label = { Text("Home") },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.home),
                    contentDescription = "Home"
                )
            }
        )
        NavigationBarItem(
            selected = currentDestination?.isRoute(Destinations.PlaylistScreen::class) == true,
            onClick = {
                navController.navigate(Destinations.PlaylistScreen)
            },
            label = { Text("Playlist") },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.playlist_play),
                    contentDescription = "Playlist"
                )
            }
        )
        NavigationBarItem(
            selected = currentDestination?.isRoute(Destinations.LibraryScreen::class) == true,
            onClick = {
                navController.navigate(Destinations.LibraryScreen)
            },
            label = { Text("Library") },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.library_music),
                    contentDescription = "Song"
                )
            }
        )
    }
}

@Preview
@Composable
fun PreviewHome() {
    AppNavLayout()
}