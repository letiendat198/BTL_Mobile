package com.ptit.btl_mobile.ui.screens.playlist

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ptit.btl_mobile.model.database.Playlist
import com.ptit.btl_mobile.ui.components.TopAppBarContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistScreen(
    onNavToCreatePlaylist: () -> Unit,
    onNavToPlaylistDetailsScreen: () -> Unit,
    onSetTopAppBar: (TopAppBarContent) -> Unit
) {
    val viewModel = viewModel<PlaylistViewModel>(
        viewModelStoreOwner = LocalActivity.current as ComponentActivity
    )
    val playlists by viewModel.playlists.collectAsState()

    var selectedPlaylist by remember { mutableStateOf<Playlist?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    onSetTopAppBar(TopAppBarContent(
        title = "Playlist"
    ))

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onNavToCreatePlaylist) {
                Icon(Icons.Default.Add, contentDescription = "Create playlist")
            }
        }
    ) { padding ->
        if (playlists.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No Playlist. Press '+' to create.")
            }
        } else {
            LazyColumn(
                contentPadding = padding,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                items(playlists) { playlist ->
                    PlaylistCard(
                        playlist = playlist,
                        onClick = {
                            viewModel.selectPlaylist(playlist)
                            viewModel.loadSongsForPlaylist(playlist.playlistId)
                            onNavToPlaylistDetailsScreen()
                        },
                        onEdit = {
                            selectedPlaylist = playlist
                            showEditDialog = true
                        },
                        onDelete = {
                            selectedPlaylist = playlist
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }
    }

    selectedPlaylist?.let {
        if (showEditDialog) {
            var editName by remember { mutableStateOf(it.name) }
            EditNameDialog(
                title = "Edit Playlist Name",
                currentName = editName,
                onNameChange = { editName = it },
                onConfirm = {
                    if (editName.isNotBlank()) {
                        viewModel.updatePlaylistName(it, editName)
                    }
                    showEditDialog = false
                },
                onDismiss = { showEditDialog = false }
            )
        }

        if (showDeleteDialog) {
            ConfirmDialog(
                title = "Delete Playlist",
                text = "Do you want to delete '${it.name}' ?",
                onConfirm = {
                    viewModel.deletePlaylist(it)
                    showDeleteDialog = false
                },
                onDismiss = { showDeleteDialog = false }
            )
        }
    }
}

@Composable
private fun EditNameDialog(
    title: String,
    currentName: String,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = currentName,
                onValueChange = onNameChange,
                label = { Text("Playlist Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun ConfirmDialog(
    title: String,
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
