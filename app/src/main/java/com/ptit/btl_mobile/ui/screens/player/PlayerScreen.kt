package com.ptit.btl_mobile.ui.screens.player

import android.text.format.DateUtils
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ptit.btl_mobile.R
import com.ptit.btl_mobile.ui.components.PlaybackControl
import com.ptit.btl_mobile.ui.components.SongList
import com.ptit.btl_mobile.ui.components.ThumbnailImage

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun PlayerScreen(
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onBack: () -> Unit,
    onNavigateToLyrics: (Long, String) -> Unit
) {
    val viewModel: PlayerViewModel = viewModel(viewModelStoreOwner = LocalActivity.current as ComponentActivity)
    var seekPosition by viewModel.currentPosition
    val currentSong by viewModel.currentSong
    var dragOffset by remember { mutableFloatStateOf(0f) }

    val recommendedSongs = viewModel.recommendedSongs

    BackHandler { onBack() }

    currentSong?.let { song ->
        with(sharedTransitionScope) {
            Scaffold { paddingValues ->
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .padding(horizontal = 25.dp)
                        .draggable(
                            orientation = Orientation.Vertical,
                            state = rememberDraggableState { delta -> dragOffset += delta },
                            onDragStopped = {
                                if (dragOffset > 0) onBack()
                                dragOffset = 0f
                            }
                        )
                ) {
                    // --- MAIN CONTENT AREA (QUEUE / PLAYER + RECOMMENDATIONS) ---
                    AnimatedContent(
                        targetState = viewModel.showQueue,
                        label = "Queue/Recommendation Animation",
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) { shouldShowQueue ->
                        if (shouldShowQueue) {
                            PlayerQueue(viewModel)
                        } else {
                            // Animation for image weight scaling
                            // If recommendations are shown -> image takes 45%, else 100%
                            val imageWeight by animateFloatAsState(
                                targetValue = if (viewModel.showRecommendations) 0.45f else 1f,
                                animationSpec = tween(durationMillis = 400),
                                label = "ImageResizeAnimation"
                            )

                            Column(modifier = Modifier.fillMaxSize()) {
                                // 1. COVER IMAGE (THUMBNAIL)
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .weight(imageWeight)
                                        .fillMaxWidth()
                                        .padding(vertical = if (viewModel.showRecommendations) 16.dp else 32.dp)
                                ) {
                                    ThumbnailImage(
                                        song.song.imageUri,
                                        modifier = Modifier
                                            .aspectRatio(1f)
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(16.dp))
                                            .sharedElement(
                                                rememberSharedContentState(key = "image"),
                                                animatedVisibilityScope = animatedContentScope
                                            )
                                    )
                                }

                                // 2. RECOMMENDATION LIST (Inside a styled Surface)
                                // Only visible when viewModel.showRecommendations is true

                                // CRITICAL FIX: Ensure weight is never exactly 0 to prevent crash
                                val listWeight = (1f - imageWeight).coerceAtLeast(0.001f)

                                AnimatedVisibility(
                                    visible = viewModel.showRecommendations,
                                    modifier = Modifier.weight(listWeight)
                                ) {
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
                        }
                    }

                    // --- CONTROLS AREA (SLIDER, BUTTONS) ---
                    Column(
                        verticalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(vertical = 16.dp)
                    ) {
                        Column {
                            Slider(
                                value = seekPosition.toFloat(),
                                onValueChange = { seekPosition = it.toLong() },
                                onValueChangeFinished = { viewModel.mediaController?.seekTo(seekPosition * 1000) },
                                valueRange = 0f..(song.song.duration / 1000).toFloat()
                            )
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
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
                        FunctionRow(viewModel, onNavigateToLyrics)
                    }
                }
            }
        }
    } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("No song selected") // Changed to English
    }
}

@Composable
fun FunctionRow(
    viewModel: PlayerViewModel,
    onNavigateToLyrics: (Long, String) -> Unit
) {
    val currentSong by viewModel.currentSong

    Row(
        horizontalArrangement = Arrangement.SpaceAround,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Lyrics Button
        IconButton(onClick = {
            currentSong?.let { onNavigateToLyrics(it.song.songId, it.song.name) }
        }) {
            Icon(
                painter = painterResource(R.drawable.lyrics),
                contentDescription = "Lyrics",
                modifier = Modifier.size(30.dp)
            )
        }

        // Toggle Recommendations Button (Star)
        IconButton(onClick = { viewModel.showRecommendations = !viewModel.showRecommendations }) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Toggle Recommendations",
                modifier = Modifier.size(30.dp),
                tint = if (viewModel.showRecommendations) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }

        // Toggle Queue Button
        IconButton(onClick = { viewModel.showQueue = !viewModel.showQueue }) {
            if (viewModel.showQueue) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Show Player",
                    modifier = Modifier.size(30.dp)
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.queue_music),
                    contentDescription = "Show Queue",
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}