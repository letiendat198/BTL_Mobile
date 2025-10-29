package com.ptit.btl_mobile.ui.screens.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ptit.btl_mobile.ui.components.SongList

@Composable
fun PlayerQueue(viewModel: PlayerViewModel) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .padding(0.dp, 10.dp)
    ) {
        Text(
            "Queue",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
        SongList(
            songs = viewModel.currentQueue.value,
            snapToCurrentSong = true
        )
    }
}