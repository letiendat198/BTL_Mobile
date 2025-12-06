package com.ptit.btl_mobile.ui.screens.playlist

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ptit.btl_mobile.ui.components.SongEntry
import com.ptit.btl_mobile.ui.components.TopAppBarContent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSongsToPlaylistScreen(
    playlistId: Long,
    onBack: () -> Unit,
    onSetTopAppBar: (TopAppBarContent) -> Unit
) {
    val viewModel: PlaylistViewModel = viewModel(
        viewModelStoreOwner = LocalActivity.current as ComponentActivity
    )

    val allSongs by viewModel.allSongs.collectAsState()
    val songsInPlaylist by viewModel.playlistSongs.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var sortByTitle by remember { mutableStateOf(true) }
    var selectedArtist by remember { mutableStateOf<String?>(null) }

    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val localSelected = remember { mutableStateListOf<Long>() }

    LaunchedEffect(playlistId) {
        viewModel.loadSongsForPlaylist(playlistId)
        viewModel.clearSongSelection()
        localSelected.clear()
    }

    // Filter by search
    var filteredSongs = allSongs.filter {
        it.song.name.contains(searchQuery, ignoreCase = true) ||
                it.artists.any { a -> a.name.contains(searchQuery, ignoreCase = true) }
    }

    // Filter by artist
    selectedArtist?.let {
        filteredSongs = filteredSongs.filter { s -> s.artists.any { it.name == selectedArtist } }
    }

    // Sort
    filteredSongs = if (sortByTitle)
        filteredSongs.sortedBy { it.song.name }
    else
        filteredSongs.sortedBy { it.artists.firstOrNull()?.name ?: "" }

    val songsInPlaylistIds = songsInPlaylist.map { it.song.songId }.toSet()

    fun toggle(id: Long) {
        if (localSelected.contains(id)) localSelected.remove(id)
        else localSelected.add(id)

        viewModel.toggleSongSelection(id)
    }

    fun selectAllFiltered() {
        filteredSongs.forEach {
            val id = it.song.songId
            if (!songsInPlaylistIds.contains(id) && !localSelected.contains(id)) {
                localSelected.add(id)
                viewModel.toggleSongSelection(id)
            }
        }
    }

    onSetTopAppBar(TopAppBarContent(
        title = "Add Songs",
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
            }
        }
    ))

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    viewModel.addSongsToExistingPlaylist(playlistId, localSelected.toList())
                    scope.launch { snackbar.showSnackbar("Added ${localSelected.size} songs") }
                    onBack()
                },
                icon = { Icon(Icons.Default.Done, null) },
                text = { Text("Done (${localSelected.size})") }
            )
        }
    ) { pad ->

        Column(Modifier.padding(pad)) {

            // Search
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, null) },
                placeholder = { Text("Search songs...") }
            )

            // Sort + Artist Filter + Select All
            Row(Modifier.padding(horizontal = 8.dp)) {
                TextButton(onClick = { sortByTitle = !sortByTitle }) {
                    Text(if (sortByTitle) "Sort: Title" else "Sort: Artist")
                }
                Spacer(Modifier.width(8.dp))
                ArtistPicker(
                    artists = allSongs.flatMap { it.artists.map { a -> a.name } }.toSet().toList(),
                    selected = selectedArtist,
                    onSelect = { selectedArtist = it }
                )

                Spacer(Modifier.weight(1f))
                TextButton(onClick = { selectAllFiltered() }) {
                    Text("Select all")
                }
            }

            if (filteredSongs.isEmpty()) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text("No results")
                }
                return@Scaffold
            }

            LazyColumn {
                items(filteredSongs) { song ->
                    val id = song.song.songId
                    val inPlaylist = songsInPlaylistIds.contains(id)
                    val selected = localSelected.contains(id)

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !inPlaylist) { toggle(id) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selected || inPlaylist,
                            onCheckedChange = { if (!inPlaylist) toggle(id) },
                            enabled = !inPlaylist
                        )
                        Spacer(Modifier.width(8.dp))
                        SongEntry(song)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArtistPicker(
    artists: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit
) {
    var showSheet by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    val filtered = remember(query, artists) {
        artists.filter { it.contains(query, ignoreCase = true) }
    }

    TextButton(onClick = { showSheet = true }) {
        Text(selected ?: "Artist")
        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
    }

    if (showSheet) {
        ModalBottomSheet(onDismissRequest = { showSheet = false }) {

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search artist...") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                // "All" option
                ListItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSelect(null)
                            showSheet = false
                        },
                    headlineContent = { Text("All") }
                )

                Spacer(Modifier.height(4.dp))

                LazyColumn {
                    items(filtered) { artist ->
                        ListItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelect(artist)
                                    showSheet = false
                                },
                            headlineContent = { Text(artist) }
                        )
                    }

                    item { Spacer(Modifier.height(20.dp)) }
                }
            }
        }
    }
}
