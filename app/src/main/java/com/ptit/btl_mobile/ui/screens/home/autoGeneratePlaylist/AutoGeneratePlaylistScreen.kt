package com.ptit.btl_mobile.ui.screens.home.autoGeneratePlaylist

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ptit.btl_mobile.model.database.SongWithArtists
import com.ptit.btl_mobile.ui.components.ThumbnailImage
import com.ptit.btl_mobile.ui.components.TopAppBarContent
import com.ptit.btl_mobile.ui.screens.playlist.PlaylistViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoGeneratePlaylistScreen(
    onBack: () -> Unit,
    onPlaylistGenerated: () -> Unit,
    onSetTopAppBar: (TopAppBarContent) -> Unit
) {
    val activity = LocalActivity.current as ComponentActivity
    val viewModel: PlaylistViewModel = viewModel(viewModelStoreOwner = activity)
    val allSongs by viewModel.allSongs.collectAsState()

    val templates = remember { PlaylistTemplates.getAll() } // from AutoPlaylistGenerator.kt

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var currentPreviewSongs by remember { mutableStateOf<List<SongWithArtists>>(emptyList()) }
    var currentTemplate by remember { mutableStateOf<PlaylistTemplate?>(null) }
    var showSheet by remember { mutableStateOf(false) }
    var playlistName by remember { mutableStateOf("") }

    // Helper: generate preview songs for a template (safe even if library empty)
    fun generatePreview(template: PlaylistTemplate): List<SongWithArtists> {
        val generated = template.generator(allSongs)
        // choose up to 8 for preview (if fewer available then all)
        return if (generated.size > 8) generated.take(8) else generated
    }

    // Suggest name based on template + sample artist
    fun suggestName(template: PlaylistTemplate, preview: List<SongWithArtists>): String {
        val sampleArtist = preview.firstOrNull()?.artists?.firstOrNull()?.name
        return if (!sampleArtist.isNullOrBlank()) {
            "${template.name} • ${sampleArtist.take(12)}"
        } else {
            "${template.name}"
        }
    }

    onSetTopAppBar(TopAppBarContent(
        title = "Auto-generate Playlist",
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            // optional action placeholder
        }
    ))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = "Choose a template",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Preview recommendations before creating. Shuffle preview if you want a different mix.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        val listState = rememberLazyListState()
        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(templates) { t ->
                TemplateCard(
                    template = t,
                    onClick = {
                        currentTemplate = t
                        currentPreviewSongs = generatePreview(t)
                        playlistName = suggestName(t, currentPreviewSongs)
                        showSheet = true
                        scope.launch { sheetState.show() }
                    }
                )
            }
            item {
                Spacer(modifier = Modifier.height(92.dp))
            }
        }
    }

    // Modal bottom sheet preview
    if (showSheet && currentTemplate != null) {
        ModalBottomSheet(
            onDismissRequest = {
                showSheet = false
                scope.launch { sheetState.hide() }
            },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(currentTemplate!!.name, style = MaterialTheme.typography.titleMedium)
                        Text(
                            currentTemplate!!.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = {
                        // shuffle preview
                        currentTemplate?.let {
                            currentPreviewSongs = generatePreview(it).shuffled()
                            playlistName = suggestName(it, currentPreviewSongs)
                            scope.launch {
                                snackbarHostState.showSnackbar("Preview shuffled")
                            }
                        }
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Shuffle preview")
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Preview list (show thumbnail + title + artist)
                Text("Preview (${currentPreviewSongs.size})", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))

                if (currentPreviewSongs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No songs available for this template", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        currentPreviewSongs.forEachIndexed { index, s ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Min)
                                    .clickable {
                                        // Optionally: play preview - not implemented to keep UI-only
                                    }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Thumbnail: use ThumbnailImage if available; fallback box
                                if (s.song.imageUri != null) {
                                    ThumbnailImage(
                                        imageUri = s.song.imageUri,
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(((index + 1).toString()), style = MaterialTheme.typography.bodyMedium)
                                    }
                                }

                                Spacer(Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = s.song.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = s.artists.joinToString { it.name },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    // duration display: convert ms -> m:ss
                                    text = formatDuration(s.song.duration),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Divider()
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = playlistName,
                    onValueChange = { playlistName = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()
                        .navigationBarsPadding()
                        .padding(bottom = 8.dp),
                    label = { Text("Playlist name") },
                    singleLine = true
                )

                Spacer(Modifier.height(8.dp))


                // quick suggestions row (3 chips)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val suggestions = generateNameSuggestions(currentTemplate!!, currentPreviewSongs)
                    suggestions.take(3).forEach { s ->
                        AssistChip(
                            onClick = { playlistName = s },
                            label = { Text(s) }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (playlistName.isBlank()) {
                            scope.launch { snackbarHostState.showSnackbar("Please enter playlist name") }
                            return@Button
                        }
                        // create playlist via ViewModel (use existing method)
                        scope.launch {
                            viewModel.createAutoGeneratedPlaylist(playlistName, currentPreviewSongs)
                            snackbarHostState.showSnackbar("Created playlist \"${playlistName}\"")
                            showSheet = false
                            currentTemplate = null
                            currentPreviewSongs = emptyList()
                            playlistName = ""
                            onPlaylistGenerated()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Create playlist (${currentPreviewSongs.size} songs)")
                }

                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

/** Small template card used in list */
@Composable
private fun TemplateCard(template: PlaylistTemplate, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(template.icon, style = MaterialTheme.typography.titleLarge)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(template.name, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    template.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/** Format milliseconds to mm:ss */
private fun formatDuration(ms: Long): String {
    if (ms <= 0L) return "0:00"
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%d:%02d".format(min, sec)
}

private fun generateNameSuggestions(template: PlaylistTemplate, preview: List<SongWithArtists>): List<String> {
    val base = template.name
    val artist = preview.firstOrNull()?.artists?.firstOrNull()?.name
    val count = preview.size
    val suggestions = mutableListOf<String>()
    suggestions.add(base)
    if (!artist.isNullOrBlank()) suggestions.add("$base • $artist")
    suggestions.add("$base (${count} songs)")
    return suggestions.distinct()
}
