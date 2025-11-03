package com.ptit.btl_mobile.ui.screens.library.detail

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ptit.btl_mobile.ui.components.ThumbnailImage
import com.ptit.btl_mobile.ui.screens.library.LibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailScreen(
    artistId: Long,
    navController: NavController
) {
    val viewModel: LibraryViewModel = viewModel(
        viewModelStoreOwner = LocalActivity.current as ComponentActivity
    )

    LaunchedEffect(artistId) {
        viewModel.loadArtistDetail(artistId)
    }

    val artist = viewModel.selectedArtist
    val albumCount = viewModel.selectedArtistAlbumCount
    val songCount = viewModel.selectedArtistSongCount
    val songs = viewModel.artistSongs
    val albums = viewModel.artistAlbums

    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(artist?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Quay lại")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Artist Header
            artist?.let { artistData ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ThumbnailImage(
                        imageUri = artistData.imageUri,
                        size = 150.dp,
                        isCircle = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = artistData.name,
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "$albumCount albums • $songCount bài hát",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Bài hát") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Albums") }
                )
            }

            when (selectedTab) {
                0 -> {
                    // Tab bài hát
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(songs.size) { index ->
                            val song = songs[index]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { /* TODO: Play song */ }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ThumbnailImage(
                                    imageUri = song.imageUri,
                                    size = 50.dp
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = song.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = formatDuration(song.duration),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Tab albums
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(albums.size) { index ->
                            val albumInfo = albums[index]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        navController.navigate("library/album/${albumInfo.album.albumId}")
                                    }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ThumbnailImage(
                                    imageUri = albumInfo.album.imageUri,
                                    size = 60.dp
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = albumInfo.album.name,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = "${albumInfo.album.year ?: ""} • ${albumInfo.songCount} bài hát",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatDuration(millis: Long): String {
    val seconds = millis / 1000
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%d:%02d", minutes, secs)
}