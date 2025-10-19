package com.ptit.btl_mobile.ui.components

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ptit.btl_mobile.R
import com.ptit.btl_mobile.model.database.Artist
import com.ptit.btl_mobile.model.database.Song
import com.ptit.btl_mobile.model.database.SongWithArtists
import com.ptit.btl_mobile.ui.theme.BTL_MobileTheme
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
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
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
        Image(
            painter = painterResource(R.drawable.ic_music_sample),
            contentDescription = "Song image",
            modifier = Modifier.size(50.dp)
        )
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxHeight()
        ) {
            Text(song.song.name, fontWeight = FontWeight.SemiBold)
            Text(song.artists.joinToString(",") { it.name }, fontWeight = FontWeight.Light)
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
            songPath = "",
            duration = 123,
            dateAdded = Date(),
            imagePath = "",
            songAlbumId = 1L,
        )
        var artist = Artist(
            artistId = i.toLong(),
            name = "Artist " + i,
            description = "Who tf is this",
            imagePath = ""
        )
        songs.add(SongWithArtists(song, listOf(artist)))
    }

    BTL_MobileTheme {
        SongList(songs)
    }
}