package com.ptit.btl_mobile.ui.screens.playlist

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ptit.btl_mobile.model.database.SongWithArtists
import com.ptit.btl_mobile.ui.components.SongEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSongsToPlaylistScreen(
    playlistId: Long,
    onBack: () -> Unit
) {
    val viewModel: PlaylistViewModel = viewModel(
        viewModelStoreOwner = LocalActivity.current as ComponentActivity
    )

    val allSongs by viewModel.allSongs.collectAsState()
    val songsInPlaylist by viewModel.playlistSongs.collectAsState()
    val selectedSongIds = remember { mutableStateListOf<Long>() }

    // Load songs for the playlist when the screen is first composed
    LaunchedEffect(playlistId) {
        viewModel.loadSongsForPlaylist(playlistId)
        // Clear any previous selections when entering the screen
        viewModel.clearSongSelection()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Songs") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        viewModel.addSongsToExistingPlaylist(playlistId, selectedSongIds)
                        onBack()
                    }) {
                        Text("DONE")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val songIdsInPlaylist = songsInPlaylist.map { it.song.songId }.toSet()

            items(allSongs, key = { it.song.songId }) { song ->
                val isSelected = selectedSongIds.contains(song.song.songId)
                val isInPlaylist = songIdsInPlaylist.contains(song.song.songId)

                SelectableSongRow(
                    song = song,
                    isSelected = isSelected,
                    isEnabled = !isInPlaylist,
                    onToggleSelection = {
                        if (!isInPlaylist) {
                            viewModel.toggleSongSelection(song.song.songId)
                            // Sync local list with ViewModel's list
                            if (selectedSongIds.contains(song.song.songId)) {
                                selectedSongIds.remove(song.song.songId)
                            } else {
                                selectedSongIds.add(song.song.songId)
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SelectableSongRow(
    song: SongWithArtists,
    isSelected: Boolean,
    isEnabled: Boolean,
    onToggleSelection: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isEnabled, onClick = onToggleSelection)
            .padding(horizontal = 8.dp)
    ) {
        Checkbox(
            checked = isSelected || !isEnabled, // Checked if selected or disabled (already in playlist)
            onCheckedChange = { onToggleSelection() },
            enabled = isEnabled
        )
        Box(modifier = Modifier.weight(1f)) {
             SongEntry(song = song, modifier = Modifier.padding(start = 8.dp))
        }
    }
}
