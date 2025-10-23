package com.ptit.btl_mobile

import android.Manifest
import android.content.ComponentName
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ptit.btl_mobile.model.database.Artist
import com.ptit.btl_mobile.model.database.Database
import com.ptit.btl_mobile.model.database.Song
import com.ptit.btl_mobile.model.database.SongArtistCrossRef
import com.ptit.btl_mobile.model.media.MediaLoader
import com.ptit.btl_mobile.model.media.PlaybackService
import com.ptit.btl_mobile.ui.components.FloatingPlayer
import com.ptit.btl_mobile.ui.screens.player.PlayerViewModel
import com.ptit.btl_mobile.ui.theme.BTL_MobileTheme
import com.ptit.btl_mobile.util.DateConverter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Date

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Init database
        Database(this.applicationContext)

        // Request permission
        // TODO: ONLY REQUEST IF NOT GRANTED> CURRENTLY RUN EVERYTIME
        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    Log.d("PERMISSION", "Permission granted")
                    MediaLoader(this, this.lifecycleScope).loadMediaIntoDB()
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

fun insertTestData() {
    val db = Database.getInstance()
    val song = Song(
        name = "Song 1",
        songUri = "",
        duration = 10,
        dateAdded = DateConverter.fromDate(Date()),
        imageUri = "",
        songAlbumId = null
    )
    val artist = Artist(
        name = "Artist 1",
        description = "Who tf is this",
        imageUri = null
    )
    GlobalScope.launch {
        val songId = db.SongDAO().insertSong(song)
        val artistId = db.ArtistDAO().insertArtist(artist)
        db.SongDAO().insertSongWithArtists(SongArtistCrossRef(artistId, songId))
        Log.d("MAIN_ACTIVITY", "Insert test data completed")
    }
}

@Composable
fun AppNavLayout() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
                NavigationBarItem(
                    selected = currentDestination?.hierarchy?.any {
                        it.hasRoute(Destinations.HomeScreen::class)
                    } == true,
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
                    selected = currentDestination?.hierarchy?.any {
                        it.hasRoute(Destinations.PlaylistScreen::class)
                    } == true,
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
                    selected = currentDestination?.hierarchy?.any {
                        it.hasRoute(Destinations.LibraryScreen::class)
                    } == true,
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
    ) { innerPadding ->
        // Like Outlet in React Router
        // Every composable within this NavHost will show up in this scaffold body
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(10.dp)
        ) {
            AppNavHost(
                navController,
                modifier = Modifier.weight(1f)
            )
            FloatingPlayer()
        }

    }
}

@Preview
@Composable
fun PreviewHome() {
    AppNavLayout()
}