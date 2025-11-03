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
fun AlbumDetailScreen(
    albumId: Long,
    navController: NavController
) {
    val viewModel: LibraryViewModel = viewModel(
        viewModelStoreOwner = LocalActivity.current as ComponentActivity
    )

    // Load album detail khi screen được tạo
    LaunchedEffect(albumId) {
        viewModel.loadAlbumDetail(albumId)
    }

    val album = viewModel.selectedAlbum
    val artistName = viewModel.selectedAlbumArtistName
    val songCount = viewModel.selectedAlbumSongCount
    val songs = viewModel.albumSongs

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(album?.name ?: "") },
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
            // Album Header
            album?.let { albumData ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ThumbnailImage(
                        imageUri = albumData.imageUri,
                        size = 200.dp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = albumData.name,
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = artistName ?: "Unknown Artist",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "${albumData.year ?: ""} • $songCount bài hát",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider()
            }

            // Song List
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
    }
}

private fun formatDuration(millis: Long): String {
    val seconds = millis / 1000
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%d:%02d", minutes, secs)
}