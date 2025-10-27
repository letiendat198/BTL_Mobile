package com.ptit.btl_mobile.ui.screens.playlist

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.ptit.btl_mobile.R
import com.ptit.btl_mobile.ui.components.SongEntry
import com.ptit.btl_mobile.ui.screens.player.PlayerViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(onBack: () -> Unit) {
    val viewModel: PlaylistViewModel = viewModel(
        viewModelStoreOwner = LocalActivity.current as ComponentActivity
    )
    val playerViewModel = viewModel<PlayerViewModel>(
        viewModelStoreOwner = LocalActivity.current as ComponentActivity
    )
    val selectedPlaylist by viewModel.selectedPlaylist.collectAsState()
    val playlistSongs by viewModel.playlistSongs.collectAsState()
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(selectedPlaylist?.name ?: "Playlist Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        selectedPlaylist?.let { playlist ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                // Header section with image and info
                item {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = playlist.imageUri ?: R.drawable.ic_music_sample,
                            contentDescription = "Playlist cover",
                            placeholder = painterResource(R.drawable.ic_music_sample),
                            error = painterResource(R.drawable.ic_music_sample),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(280.dp)
                                .clip(MaterialTheme.shapes.medium)
                        )

                        Spacer(Modifier.height(16.dp))

                        Text(playlist.name, style = MaterialTheme.typography.headlineMedium)
                        Text(
                            "Created: ${dateFormatter.format(playlist.dateCreated)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Text(
                            "${playlistSongs.size} songs",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(Modifier.height(16.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(onClick = { showEditDialog = true }) {
                                Text("Edit")
                            }
                            OutlinedButton(onClick = { showDeleteDialog = true }) {
                                Text("Delete")
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(8.dp))
                    }
                }

                // Empty state or song items
                if (playlistSongs.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No songs in this playlist yet",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    // Add song items directly to this LazyColumn
                    itemsIndexed(playlistSongs) { index, song ->
                        SongEntry(
                            song = song,
                            modifier = Modifier
                                .clickable {
                                    playerViewModel.playSong(index, playlistSongs)
                                }
                                .padding(horizontal = 16.dp)
                        )
                    }
                }
            }

            // Edit Dialog
            if (showEditDialog) {
                var editName by remember { mutableStateOf(playlist.name) }
                AlertDialog(
                    onDismissRequest = { showEditDialog = false },
                    title = { Text("Edit Playlist Name") },
                    text = {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Playlist name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(onClick = {
                            if (editName.isNotBlank()) {
                                viewModel.updatePlaylistName(playlist, editName)
                            }
                            showEditDialog = false
                        }) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showEditDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // Delete Dialog
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Delete Playlist") },
                    text = { Text("Are you sure you want to delete '${playlist.name}'?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.deletePlaylist(playlist)
                                showDeleteDialog = false
                                onBack()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Confirm")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        } ?: Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator()
                Text("Loading playlist...")
            }
        }
    }
}