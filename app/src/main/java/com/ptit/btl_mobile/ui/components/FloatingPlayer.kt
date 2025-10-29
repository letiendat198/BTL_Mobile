package com.ptit.btl_mobile.ui.components

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ptit.btl_mobile.ui.screens.player.PlayerViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun FloatingPlayer(
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onShowPlayer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = viewModel<PlayerViewModel>(viewModelStoreOwner = LocalActivity.current as ComponentActivity)
    val currentSong by viewModel.currentSong
    // Only used to disable ripple on click
    val interactionSource = remember { MutableInteractionSource() }
    var dragOffset by remember { mutableFloatStateOf(0f) }

    currentSong?.let {
        with(sharedTransitionScope) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = modifier
                    .fillMaxWidth()
                    .clip(shape = RoundedCornerShape(5.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(5.dp)
                    .draggable(
                        orientation = Orientation.Vertical,
                        state = rememberDraggableState { delta -> dragOffset += delta },
                        onDragStopped = {
                            if (dragOffset < 0) { // Down is +
                                onShowPlayer()
                            }
                            dragOffset = 0f; // Reset
                        }
                    )
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = modifier.weight(1f)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            onShowPlayer()
                        }
                ) {
                    ThumbnailImage(
                        imageUri = it.song.imageUri,
                        modifier = Modifier
                            .size(50.dp)
                            .sharedElement(
                                rememberSharedContentState(key = "image"),
                                animatedVisibilityScope = animatedContentScope
                            )
                    )
                    Column(
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            it.song.name,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.basicMarquee()
                        )
                        Text(
                            if (it.artists.isNotEmpty())
                                it.artists.joinToString(",") { it.name } else "Unknown artists",
                            fontWeight = FontWeight.Light,
                            modifier = Modifier.basicMarquee()
                        )
                    }
                }
                PlaybackControl(viewModel,
                    controlSize = 40.dp,
                    modifier = Modifier.sharedElement(
                        // Don't use imageUri because it may be null
                        rememberSharedContentState(key = "control"),
                        animatedVisibilityScope = animatedContentScope
                    )
                )
            }
        }
    }
}

//@Preview(showBackground = true, showSystemUi = true)
//@Composable
//fun PlaybackControlPreview() {
//    FloatingPlayer()
//}