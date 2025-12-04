package com.ptit.btl_mobile.ui.screens.home.autoGeneratePlaylist

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ptit.btl_mobile.model.database.SongWithArtists
import com.ptit.btl_mobile.ui.screens.playlist.PlaylistViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoGeneratePlaylistScreen(
    onBack: () -> Unit,
    onPlaylistGenerated: () -> Unit
) {
    val viewModel: PlaylistViewModel = viewModel(
        viewModelStoreOwner = LocalActivity.current as ComponentActivity
    )
    val allSongs by viewModel.allSongs.collectAsState()

    var showNameDialog by remember { mutableStateOf(false) }
    var selectedTemplate by remember { mutableStateOf<PlaylistTemplate?>(null) }
    var generatedSongs by remember { mutableStateOf<List<SongWithArtists>>(emptyList()) }

    val templates = remember { PlaylistTemplates.getAll() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Auto Generate Playlist") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header info
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Choose a template",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Auto-generate playlists based on your music library",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }

            // Templates grid
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(templates) { template ->
                    TemplateCard(
                        template = template,
                        onClick = {
                            selectedTemplate = template
                            generatedSongs = template.generator(allSongs)
                            showNameDialog = true
                        }
                    )
                }
            }
        }
    }

    // Dialog để đặt tên playlist
    if (showNameDialog && selectedTemplate != null) {
        var playlistName by remember { mutableStateOf(selectedTemplate!!.name) }

        AlertDialog(
            onDismissRequest = {
                showNameDialog = false
                selectedTemplate = null
            },
            title = { Text("Create Playlist") },
            text = {
                Column {
                    Text(
                        text = "Generated ${generatedSongs.size} songs",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = playlistName,
                        onValueChange = { playlistName = it },
                        label = { Text("Playlist Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (playlistName.isNotBlank() && generatedSongs.isNotEmpty()) {
                            // Tạo playlist với các bài hát đã generate
                            viewModel.createAutoGeneratedPlaylist(
                                name = playlistName,
                                songs = generatedSongs
                            )
                            showNameDialog = false
                            selectedTemplate = null
                            onPlaylistGenerated()
                        }
                    },
                    enabled = playlistName.isNotBlank() && generatedSongs.isNotEmpty()
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showNameDialog = false
                    selectedTemplate = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun TemplateCard(
    template: PlaylistTemplate,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon/Emoji
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = template.icon,
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            Spacer(Modifier.width(16.dp))

            // Text info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = template.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = template.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}