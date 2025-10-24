package com.ptit.btl_mobile.ui.components

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.compose.state.PlayPauseButtonState
import androidx.media3.ui.compose.state.rememberPlayPauseButtonState
import com.ptit.btl_mobile.R
import com.ptit.btl_mobile.ui.screens.player.PlayerViewModel

@OptIn(UnstableApi::class)
@Composable
fun PlaybackControl(viewModel: PlayerViewModel, modifier: Modifier = Modifier, controlSize: Dp = 48.dp) {
    // IconButton max size is 48.dp
    val buttonSize = remember { controlSize }
    val playPauseButtonState: PlayPauseButtonState? = viewModel.mediaController?.let { rememberPlayPauseButtonState(it) }

    Row(horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier) {
        IconButton(
            onClick = {
                viewModel.mediaController?.seekToPrevious()
            },
            modifier = Modifier.size(buttonSize)
        ) {
            Icon(painter = painterResource(R.drawable.skip_previous),
                contentDescription = "Previous",
                modifier = Modifier.size(buttonSize))
        }
        IconButton(
            onClick = {
                if (playPauseButtonState == null || !playPauseButtonState.showPlay) {
                    viewModel.mediaController?.pause()
                } else viewModel.mediaController?.play()
            },
            modifier = Modifier.size(buttonSize)
        ) {
            Icon(painter = if (playPauseButtonState == null || !playPauseButtonState.showPlay) {
                painterResource(R.drawable.pause)
            } else painterResource(R.drawable.play_arrow),
                contentDescription = "Pause/Play",
                modifier = Modifier.size(buttonSize))
        }
        IconButton(
            onClick = {
                // TODO: seekToNext maybe?
                viewModel.mediaController?.seekToNextMediaItem()
            },
            modifier = Modifier.size(buttonSize)
        ) {
            Icon(painter = painterResource(R.drawable.skip_next),
                contentDescription = "Next",
                modifier = Modifier.size(buttonSize))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewControl() {
//    PlaybackControl()
}