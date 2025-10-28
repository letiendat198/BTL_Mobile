package com.ptit.btl_mobile.ui.screens.player

import android.text.format.DateUtils
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
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
import androidx.compose.runtime.mutableStateOf
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun PlayerScreen(
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onBack: () -> Unit
) {
    val viewModel = viewModel<PlayerViewModel>(viewModelStoreOwner = LocalActivity.current as ComponentActivity)
    // DO NOT REMEMBER STUFFS FROM VIEWMODEL
    var seekPosition by viewModel.currentPosition
    val currentSong by viewModel.currentSong
    var dragOffset by remember { mutableFloatStateOf(0f) }

    BackHandler {
        onBack()
    }

    currentSong?.let { song ->
        with(sharedTransitionScope) {
            Scaffold { innerPadding ->
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(25.dp, 10.dp)
                        .draggable(
                            orientation = Orientation.Vertical,
                            state = rememberDraggableState { delta -> dragOffset += delta },
                            onDragStopped = {
                                if (dragOffset > 0) { // Up is +
                                    onBack()
                                }
                                dragOffset = 0f; // Reset
                            }
                        )
                ) {
                    AnimatedContent(
                        viewModel.showQueue,
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    ) { shouldShowQueue ->
                        if (!shouldShowQueue) {
                            Column {
                                Box(contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .weight(1F)
                                        .fillMaxWidth()) {
                                    ThumbnailImage( // TODO: WHY NOT ROUNDED
                                        song.song.imageUri,
                                        modifier = Modifier
                                            .fillMaxWidth() // fillMaxSize won't show rounded corner
                                            .sharedElement(
                                                rememberSharedContentState(key = "image"),
                                                animatedVisibilityScope = animatedContentScope
                                            )
                                    )
                                }
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
                            }
                        }
                        else {
                            PlayerQueue(viewModel)
                        }
                    }
                    Column(verticalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .weight(0.7f)
                            .fillMaxWidth()
                    ) {
                        Column {
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

                        PlaybackControl(
                            viewModel,
                            controlSize = 60.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .sharedElement(
                                    rememberSharedContentState(key = "control"),
                                    animatedVisibilityScope = animatedContentScope
                                )
                        )

                        FunctionRow(viewModel)
                    }
                }
            }
        }
    } ?: Text("No current song to play. This should not happen")
}

@Composable
fun FunctionRow(viewModel: PlayerViewModel) {
    Row(horizontalArrangement = Arrangement.Absolute.SpaceBetween,
        modifier = Modifier.fillMaxWidth()) {
        IconButton(
            onClick = {},
        ) {
            Icon(painter = painterResource(R.drawable.lyrics),
                contentDescription = "Lyrics",
                modifier = Modifier.size(30.dp)
            )
        }
        IconButton(
            onClick = {
                viewModel.showQueue = !viewModel.showQueue
            }
        ) {
            Icon(painter = painterResource(R.drawable.queue_music),
                contentDescription = "Queue music",
                modifier = Modifier.size(30.dp)
            )
        }
    }
}