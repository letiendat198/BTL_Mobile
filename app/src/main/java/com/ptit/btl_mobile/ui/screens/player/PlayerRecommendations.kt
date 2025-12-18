package com.ptit.btl_mobile.ui.screens.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ptit.btl_mobile.ui.components.SongEntry
import com.ptit.btl_mobile.ui.components.SongList

@Composable
fun PlayerRecommendations(viewModel: PlayerViewModel) {
    val recommendedSongs by viewModel.recommendedSongs
    val currentSong by viewModel.currentSong

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(0.dp, 10.dp)
    ) {
        Text(
            text = "Recommended Songs",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )

        SongList(recommendedSongs)

//        LazyColumn(
//            verticalArrangement = Arrangement.spacedBy(10.dp)
//        ) {
//            itemsIndexed(recommendedSongs) { index, item ->
//                SongEntry(
//                    song = item,
//                    modifier = Modifier.clickable {
//                        viewModel.playSong(index, recommendedSongs)
//                    },
//                    isPlaying = item.song.songId == currentSong?.song?.songId
//                )
//            }
//        }
    }
}