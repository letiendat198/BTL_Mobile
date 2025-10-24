package com.ptit.btl_mobile.ui.components

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ptit.btl_mobile.ui.screens.player.PlayerViewModel

@Composable
fun FloatingPlayer(modifier: Modifier = Modifier, onNavigateToPlayer: () -> Unit = {}) {
    val viewModel = viewModel<PlayerViewModel>(viewModelStoreOwner = LocalActivity.current as ComponentActivity)

    // Only used to disable ripple on click
    val interactionSource = remember { MutableInteractionSource() }

    viewModel.currentSong.value?.let {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(5.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(5.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = modifier.weight(1f)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        onNavigateToPlayer()
                    }
            ) {
                ThumbnailImage(
                    imageUri = it.song.imageUri,
                    modifier = Modifier.size(50.dp)
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
            PlaybackControl(viewModel, controlSize = 40.dp)
        }
    }


}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PlaybackControlPreview() {
    FloatingPlayer()
}