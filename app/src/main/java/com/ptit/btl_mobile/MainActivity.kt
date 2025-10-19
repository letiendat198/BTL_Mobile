package com.ptit.btl_mobile

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.ptit.btl_mobile.model.database.Artist
import com.ptit.btl_mobile.model.database.Database
import com.ptit.btl_mobile.model.database.Song
import com.ptit.btl_mobile.model.database.SongArtistCrossRef
import com.ptit.btl_mobile.ui.theme.BTL_MobileTheme
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Date

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Init database
        Database(this.applicationContext)
        insertTestData()

        setContent {
            BTL_MobileTheme {
                AppNavLayout()
            }
        }
    }
}

fun insertTestData() {
    val db = Database.getInstance()
    val song = Song(
        name = "Song 1",
        songPath = "",
        duration = 10,
        dateAdded = Date(),
        imagePath = "",
        songAlbumId = null
    )
    val artist = Artist(
        name = "Artist 1",
        description = "Who tf is this",
        imagePath = null
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
        AppNavHost(
            navController,
            Modifier
                .padding(innerPadding)
                .padding(10.dp)
        )
    }
}

@Preview
@Composable
fun PreviewHome() {
    AppNavLayout()
}