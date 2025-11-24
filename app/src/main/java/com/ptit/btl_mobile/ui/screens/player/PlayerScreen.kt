package com.ptit.btl_mobile.ui.screens.player

import android.text.format.DateUtils
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    // Lấy danh sách gợi ý từ ViewModel
    val recommendedSongs = viewModel.recommendedSongs

    BackHandler { onBack() }

    currentSong?.let { song ->
        with(sharedTransitionScope) {
            Scaffold {
                Column(
                    modifier = Modifier
                        .padding(it)
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
                            Column {
                                // 1. Ảnh bìa
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .weight(0.4f) // Giảm trọng số
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp)
                                ) {
                                    ThumbnailImage(
                                        song.song.imageUri,
                                        modifier = Modifier
                                            .aspectRatio(1f)
                                            .fillMaxSize()
                                            .sharedElement(
                                                rememberSharedContentState(key = "image"),
                                                animatedVisibilityScope = animatedContentScope
                                            )
                                    )
                                }

                                // 2. Tên bài hát và nghệ sĩ
                                Text(
                                    song.song.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    modifier = Modifier.basicMarquee()
                                )
                                Text(
                                    song.artists.joinToString(", ") { artist -> artist.name },
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = 1,
                                    modifier = Modifier.basicMarquee()
                                )
                                Spacer(Modifier.height(8.dp))

                                Box(modifier = Modifier.weight(0.6f)) {
                                    SongList(
                                        songs = recommendedSongs,
                                        header = {
                                            Text(
                                                "Suggested Songs",
                                                style = MaterialTheme.typography.titleMedium,
                                                modifier = Modifier.padding(vertical = 8.dp)
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

                    // Thanh điều khiển phát nhạc ( giữ nguyên )
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
        Text("Chưa có bài hát nào được chọn")
    }
}

@Composable
fun FunctionRow(
    viewModel: PlayerViewModel,
    onNavigateToLyrics: (Long, String) -> Unit
) {
    val currentSong by viewModel.currentSong

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        IconButton(onClick = {
            currentSong?.let { onNavigateToLyrics(it.song.songId, it.song.name) }
        }) {
            Icon(painter = painterResource(R.drawable.lyrics), contentDescription = "Lyrics", modifier = Modifier.size(30.dp))
        }
        IconButton(onClick = { viewModel.showQueue = !viewModel.showQueue }) {
            if (viewModel.showQueue) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Show Recommendations",
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
