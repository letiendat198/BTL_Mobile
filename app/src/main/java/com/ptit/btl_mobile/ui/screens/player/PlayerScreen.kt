package com.ptit.btl_mobile.ui.screens.player

import android.text.format.DateUtils
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ptit.btl_mobile.R
import com.ptit.btl_mobile.ui.components.PlaybackControl
import com.ptit.btl_mobile.ui.components.ThumbnailImage
import com.ptit.btl_mobile.ui.theme.BTL_MobileTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen() {
    val viewModel = viewModel<PlayerViewModel>(viewModelStoreOwner = LocalActivity.current as ComponentActivity)
    var seekPosition by remember { viewModel.currentPosition }
    val currentSong by remember { viewModel.currentSong }

    currentSong?.let { song ->
        Scaffold { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding).padding(25.dp, 5.dp)) {
                Box(contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1F)
                        .fillMaxWidth()) {
                    ThumbnailImage( // TODO: WHY NOT ROUNDED
                        song.song.imageUri,
                        modifier = Modifier
                            .fillMaxSize()
                    )
                }
                Column(verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Column {
                        Row (
                            modifier = Modifier.padding(0.dp, 10.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    song.song.name,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 25.sp,
                                    modifier = Modifier.basicMarquee()
                                )
                                Text(
                                    song.artists.joinToString(", ") {it.name},
                                    modifier = Modifier.basicMarquee()
                                )
                            }
                        }
                        Slider(
                            value = seekPosition.toFloat(),
                            onValueChange = {seekPosition = it.toLong()},
                            onValueChangeFinished = {
                                viewModel.mediaController?.seekTo(seekPosition * 1000) // Second to MS
                            },
                            valueRange = 0f..(song.song.duration / 1000).toFloat()
                        )
                        Row(horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            Text(DateUtils.formatElapsedTime(seekPosition))
                            Text(DateUtils.formatElapsedTime(song.song.duration / 1000))
                        }
                    }

                    PlaybackControl(viewModel, controlSize = 60.dp, modifier = Modifier.fillMaxWidth())

                    Row(horizontalArrangement = Arrangement.Absolute.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()) {
                        IconButton(
                            onClick = {},
                            // TODO: Figure out Lyric icon clipping
//                            modifier = Modifier.wrapContentSize()
                        ) {
                            Icon(painter = painterResource(R.drawable.lyrics),
                                contentDescription = "Lyrics",
                                modifier = Modifier.size(48.dp)
                            )
                        }
                        IconButton(
                            onClick = {}
                        ) {
                            Icon(painter = painterResource(R.drawable.queue_music),
                                contentDescription = "Queue music",
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }
            }
        }
    } ?: Text("No current song to play. This should not happen")
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GreetingPreview() {
    BTL_MobileTheme {
        PlayerScreen()
    }
}