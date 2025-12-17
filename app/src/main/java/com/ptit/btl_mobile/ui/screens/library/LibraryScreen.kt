package com.ptit.btl_mobile.ui.screens.library

import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ptit.btl_mobile.R
import com.ptit.btl_mobile.util.SongOption
import com.ptit.btl_mobile.ui.components.SongList
import com.ptit.btl_mobile.ui.components.TopAppBarContent
import com.ptit.btl_mobile.ui.screens.library.tabs.AlbumsTab
import com.ptit.btl_mobile.ui.screens.library.tabs.ArtistsTab
import com.ptit.btl_mobile.ui.screens.playlist.PlaylistViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.ptit.btl_mobile.model.database.SongWithArtists
import com.ptit.btl_mobile.ui.screens.playlist.PlaylistCard

enum class LibraryTabs(val index: Int, val title: String) {
    SONG(0, "Songs"),
    ALBUM(1, "Albums"),
    ARTIST(2, "Artists")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onSetTopAppBar: (TopAppBarContent) -> Unit = {},
    onNavToAlbumDetail: (album: AlbumWithInfo) -> Unit,
    onNavToArtistDetail: (artist: ArtistWithInfo) -> Unit,
    onNavToEditMetadata: (songId: Long) -> Unit
) {
    // Using MainActivity store owner allow view model
    // to be maintained even after user navigated away
    val viewModel: LibraryViewModel = viewModel(
        viewModelStoreOwner = LocalActivity.current as ComponentActivity
    )
    Log.d("LIBRARY_SCREEN", "Recomposed when song list size is: " + viewModel.songs.value.size)

    var selectedTab by viewModel.selectedTab
    var isRefreshing by remember { mutableStateOf(false) }

    onSetTopAppBar(TopAppBarContent(
        title = "Library",
        actions = {
            IconButton(
                onClick = {
                    viewModel.reloadMedia()
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.sync),
                    contentDescription = "Reload library"
                )
            }
        }
    ))

    Column {
        PrimaryTabRow(
            selectedTabIndex = selectedTab
        ) {
            enumValues<LibraryTabs>().forEach { libraryTab ->
                Tab(
                    selected = selectedTab == libraryTab.index,
                    onClick = {
                        selectedTab = libraryTab.index
                    },
                    text = {
                        Text(libraryTab.title)
                    }
                )
            }
        }

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                when(selectedTab) {
                    0 -> viewModel.getAllSongs()
                    1 -> viewModel.getAllAlbums()
                    2 -> viewModel.getAllArtists()
                }
                val scope = CoroutineScope(Dispatchers.Main)
                scope.launch {
                    delay(500L)
                    isRefreshing = false
                }
            }
        ) {
            when(selectedTab) {
                0 -> LibrarySongTab(viewModel, onNavToEditMetadata)
                1 -> AlbumsTab(
                    onNavToAlbumDetail = onNavToAlbumDetail,
                    viewModel = viewModel
                )
                2 -> ArtistsTab(
                    onNavToArtistDetail = onNavToArtistDetail,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun LibrarySongTab(
    viewModel: LibraryViewModel,
    onNavToEditMetadata: (Long) -> Unit
) {
    var searchQuery by viewModel.searchQuery
    // Persist song list state in Library view model
    viewModel.listState = viewModel.listState ?: rememberLazyListState()
    val playlistViewModel: PlaylistViewModel = viewModel()

    var showPlaylistSelectDialog by remember { mutableStateOf(false) }
    var currentSongToAdd by remember { mutableStateOf<SongWithArtists?>(null) }

    val entryOptions = listOf(
        SongOption(
            title = "Add to playlist...",
            icon = {Icon(painterResource(R.drawable.playlist_play), contentDescription = "Add to playlist")},
            onClick = { song ->
                currentSongToAdd = song
                showPlaylistSelectDialog = true
            }
        ),
        SongOption(
            title = "Edit metadata",
            icon = { Icon(Icons.Default.Edit, contentDescription = "Edit metadata") },
            onClick = { song -> onNavToEditMetadata(song.song.songId)}
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
//            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.filterSongName(it)
                },
                maxLines = 1,
                placeholder = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.search),
                            contentDescription = "Search icon"
                        )
                        Text("Search library")
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        Icon(
                            painter = painterResource(R.drawable.close_small),
                            contentDescription = "Clear search",
                            modifier = Modifier.clickable {
                                searchQuery = ""
                                viewModel.filterSongName("")
                            }
                        )
                    }
                },
                modifier = Modifier
                    .padding(0.dp, 10.dp)
                    .clip(shape = RoundedCornerShape(10.dp))
            )
            IconButton(
                onClick = {
                    viewModel.sortSong() // TODO: TEMP
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.sort),
                    contentDescription = "Sort"
                )
            }
        }

        SongList(
            songs = viewModel.songs.value,
            customState = viewModel.listState,
            entryOptions = entryOptions
        )
    }

    if (showPlaylistSelectDialog && currentSongToAdd != null) {
        val playlists = playlistViewModel.playlists.collectAsState().value
        val checkedPlaylistId = mutableListOf<Long>()
        val localContext = LocalContext.current

        Dialog(
            onDismissRequest = { showPlaylistSelectDialog = false }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column (
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.padding(15.dp)
                ) {
                    Text(
                        text = "Add song to playlists",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp
                    )
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        items(playlists) { playlist ->
                            var isChecked by remember { mutableStateOf(false) }
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        isChecked = checked
                                        if (checked) checkedPlaylistId.add(playlist.playlistId)
                                        else checkedPlaylistId.remove(playlist.playlistId)
                                    }
                                )
                                PlaylistCard(
                                    playlist = playlist,
                                    onClick = {},
                                    onEdit = {},
                                    onDelete = {},
                                )
                            }

                        }
                    }
                    Row (
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(
                            onClick = {showPlaylistSelectDialog = false}
                        ) { Text("Cancel") }
                        TextButton(
                            onClick = {
                                checkedPlaylistId.forEach { id ->
                                    playlistViewModel
                                        .addSongsToExistingPlaylist(id, listOf(currentSongToAdd!!.song.songId))
                                }
                                showPlaylistSelectDialog = false
                            }
                        ) { Text("Confirm") }
                    }
                }
            }
        }
    }
}