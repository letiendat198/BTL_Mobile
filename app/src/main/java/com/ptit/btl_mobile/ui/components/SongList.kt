package com.ptit.btl_mobile.ui.components

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ptit.btl_mobile.MainViewModel
import com.ptit.btl_mobile.R
import com.ptit.btl_mobile.model.database.Artist
import com.ptit.btl_mobile.model.database.Song
import com.ptit.btl_mobile.model.database.SongWithArtists
import com.ptit.btl_mobile.ui.screens.player.PlayerViewModel
import com.ptit.btl_mobile.ui.theme.BTL_MobileTheme
import com.ptit.btl_mobile.util.DateConverter
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongList(
    songs: List<SongWithArtists>,
    isSelecting: Boolean = false,
    snapToCurrentSong: Boolean = false,
    customState: LazyListState? = null,
    header: @Composable () -> Unit = {},
    onSelectChange: (selectedSong: Map<SongWithArtists, Boolean>) -> Unit = {},
    onClick: (song: SongWithArtists) -> Unit = {},
    entryOptions: List<Option> = listOf()
) {
    val checkStates = remember { SnapshotStateList(songs.size) {false} }
    var selectedSongs = remember {mapOf<SongWithArtists, Boolean>()}
    val listState = customState?:rememberLazyListState()

    // TODO: CAREFULL!! HERE WE ASSUME THIS COMPOSABLE IS BOUND TO AN ACTIVITY
    // IT SHOULD BE!
    val viewModel = viewModel<PlayerViewModel>(
        viewModelStoreOwner = LocalActivity.current as ComponentActivity
    )
    val mainViewModel: MainViewModel = viewModel(
        viewModelStoreOwner = LocalActivity.current as ComponentActivity
    )

    LaunchedEffect(Unit) {
        if (snapToCurrentSong && songs === viewModel.currentQueue.value) {
            val currentIndex = viewModel.currentSongIndex
            if (currentIndex > -1) listState.scrollToItem(currentIndex)
        }
    }

    LazyColumn(
//        verticalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier.fillMaxHeight(),
        state = listState
    ) {
        item {
            header()
        }
        itemsIndexed(songs) { index, song ->
            Row {
                if (isSelecting) {
                    Checkbox(
                        checked = checkStates[index],
                        onCheckedChange = { checked ->
                            checkStates[index] = checked
                            if (checked) selectedSongs += song to true
                            else selectedSongs -= song

                            onSelectChange(selectedSongs)
                        }
                    )
                }
                SongEntry(
                    song = song,
                    modifier = Modifier
                        .clickable(!isSelecting) {
                            viewModel.playSong(index, songs)
                            // TODO: REMOVE ON CLICK?
                            onClick(song)
                        },
                    isPlaying = song.song.songId == viewModel.currentSong.value?.song?.songId,
                    onOptionClick = {
                        mainViewModel.showMenuWithOptions(entryOptions)
                    }
                )
            }
        }
    }
}

@Composable
fun SongEntry(
    song: SongWithArtists,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
    onOptionClick: () -> Unit = {}
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(shape = RoundedCornerShape(5.dp))
            .then(other =
                if (isPlaying) Modifier.background(MaterialTheme.colorScheme.primaryContainer)
                else Modifier
            )
            .padding(5.dp)
            .fillMaxWidth()
    ) {
        ThumbnailImage(
            imageUri = song.song.imageUri,
            modifier = Modifier.size(50.dp)
        )
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                song.song.name,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                if (song.artists.isNotEmpty())
                    song.artists.joinToString(",") { it.name } else "Unknown artists",
                fontWeight = FontWeight.Light,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (isPlaying) {
            Icon(
                painter = painterResource(R.drawable.equalizer),
                contentDescription = "Playing",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(30.dp).padding(5.dp)
            )
        }

        Icon(
            painter = painterResource(R.drawable.more_vert),
            contentDescription = "Options",
            modifier = Modifier
                .size(30.dp)
                .padding(5.dp)
                .clickable(onClick = onOptionClick)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GreetingPreview() {
    val songs = mutableListOf<SongWithArtists>()
    for (i in 1..10) {
        var song = Song(
            i.toLong(),
            name = "Song " + i,
            songUri = "",
            duration = 123,
            dateAdded = DateConverter.fromDate(Date()),
            songAlbumId = 1L,
        )
        var artist = Artist(
            artistId = i.toLong(),
            name = "Artist " + i,
            description = "Who tf is this",
        )
        songs.add(SongWithArtists(song, listOf(artist)))
    }

    BTL_MobileTheme {
        SongList(songs)
    }
}