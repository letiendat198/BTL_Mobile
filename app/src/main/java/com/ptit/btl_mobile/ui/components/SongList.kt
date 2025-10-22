package com.ptit.btl_mobile.ui.components

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.ptit.btl_mobile.MainActivity
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
    onSelectChange: (selectedSong: Map<SongWithArtists, Boolean>) -> Unit = {},
    onClick: (song: SongWithArtists) -> Unit = {}
) {
    val checkStates = remember { SnapshotStateList(songs.size) {false} }
    var selectedSongs = remember {mapOf<SongWithArtists, Boolean>()}

    // TODO: CAREFULL!! HERE WE ASSUME THIS COMPOSABLE IS BOUND TO AN ACTIVITY
    // IT SHOULD BE!
    val viewModel = viewModel<PlayerViewModel>(viewModelStoreOwner = LocalActivity.current as ComponentActivity)

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier.fillMaxHeight()
    ) {
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
                SongEntry(song, Modifier.clickable(!isSelecting) {
                    viewModel.currentSong.value = song
                    viewModel.updateCurrentQueue(songs)
                    // TODO: REMOVE ON CLICK?
                    onClick(song)
                })
            }
        }
    }
}

@Composable
fun SongEntry(song: SongWithArtists, modifier: Modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        SongImage(
            imageUri = song.song.imageUri,
            modifier = Modifier.size(50.dp)
        )
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(song.song.name, fontWeight = FontWeight.SemiBold)
            Text(
                if (song.artists.isNotEmpty())
                    song.artists.joinToString(",") { it.name } else "Unknown artists",
                fontWeight = FontWeight.Light)
        }
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