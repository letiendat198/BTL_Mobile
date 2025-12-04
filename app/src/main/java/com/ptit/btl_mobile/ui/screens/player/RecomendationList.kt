package com.ptit.btl_mobile.ui.screens.player

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ptit.btl_mobile.ui.components.SongList

@Composable
fun RecommendationList(
    viewModel: PlayerViewModel
) {
    val recommendedSongs = viewModel.recommendedSongs
    val currentSong by viewModel.currentSong
    currentSong?.let { song ->
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                // Song Info (Marquee)
                Text(
                    song.song.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.basicMarquee()
                )
                Text(
                    song.artists.joinToString(", ") { artist -> artist.name },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    modifier = Modifier.basicMarquee()
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // Recommendation List
                Box(modifier = Modifier.weight(1f)) {
                    SongList(
                        songs = recommendedSongs,
                        header = {
                            Text(
                                "Recommended for you", // Changed to English
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        },
                        onClick = { clickedSong ->
                            val index = recommendedSongs.indexOf(clickedSong)
                            if (index != -1) {
                                viewModel.playSong(index, recommendedSongs)
                            }
                        }
                    )
                }
            }
        }
    }

}