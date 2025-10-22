package com.ptit.btl_mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ptit.btl_mobile.ui.screens.player.PlayerViewModel

@Composable
fun PlaybackControl(modifier: Modifier = Modifier) {
    // TODO: Explicitly make MainActivity own this. May cause problem later if moved
    val viewModel = viewModel<PlayerViewModel>()

    if (viewModel.currentSong.value != null) {
        val song = viewModel.currentSong.value
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(5.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(5.dp)
        ) {
            SongImage(
                imageUri = song?.song?.imageUri,
                modifier = Modifier.size(50.dp)
            )
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(song?.song?.name ?: "This should not happen", fontWeight = FontWeight.SemiBold)
                Text(
                    if (song?.artists?.isNotEmpty() ?: false)
                        song.artists.joinToString(",") { it.name } else "Unknown artists",
                    fontWeight = FontWeight.Light)
            }
        }
    }


}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PlaybackControlPreview() {
    PlaybackControl()
}