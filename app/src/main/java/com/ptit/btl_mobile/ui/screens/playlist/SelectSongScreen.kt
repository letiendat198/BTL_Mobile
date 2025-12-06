package com.ptit.btl_mobile.ui.screens.playlist

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ptit.btl_mobile.ui.components.SongList
import com.ptit.btl_mobile.ui.components.TopAppBarContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectSongsScreen(onBack: () -> Unit = {}, onSetTopAppBar: (TopAppBarContent) -> Unit) {
    val viewModel = viewModel<PlaylistViewModel>(
        viewModelStoreOwner = LocalActivity.current as ComponentActivity
    )
    val allSongs by viewModel.allSongs.collectAsState()
    val draft by viewModel.playlistDraft.collectAsState()

    onSetTopAppBar(TopAppBarContent(
        title = "Select Songs",
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            TextButton(
                onClick = {
                    // Tạo playlist và quay về
                    viewModel.confirmCreatePlaylist()
                    onBack()
                },
                enabled = viewModel.selectedSongIds.isNotEmpty()
            ) {
                Text("Done (${viewModel.selectedSongIds.size})")
            }
        }
    ))

    Column() {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Creating: ${draft.name}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Select songs for this playlist",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }

        SongList(
            songs = allSongs,
            isSelecting = true,
            onSelectChange = { selectedMap ->
                val selectedIds = selectedMap.keys.map { it.song.songId }
                viewModel.updateDraftSelectedSongs(selectedIds)
            },
        )
    }
}