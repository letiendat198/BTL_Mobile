package com.ptit.btl_mobile.ui.screens.playlist

import android.net.Uri
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ptit.btl_mobile.ui.components.ThumbnailImage
import com.ptit.btl_mobile.ui.components.TopAppBarContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePlaylistScreen(
    onBack: () -> Unit,
    onNavToSelectSongs: () -> Unit,
    onSetTopAppBar: (TopAppBarContent) -> Unit
) {
    val context = LocalContext.current
    val viewModel = viewModel<PlaylistViewModel>(
        viewModelStoreOwner = LocalActivity.current as ComponentActivity
    )

    val draft by viewModel.playlistDraft.collectAsState()
    val allSongs by viewModel.allSongs.collectAsState()

    var playlistName by remember { mutableStateOf(draft.name) }
    var imageUri by remember { mutableStateOf<String?>(draft.imageUri) }
    var tempSelectedUri by remember { mutableStateOf<Uri?>(draft.tempImageUri) }

    onSetTopAppBar(TopAppBarContent(
        title = "Create New Playlist",
        navigationIcon = {
            IconButton(onClick = {
                viewModel.clearDraft()
                onBack()
            }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            TextButton(
                onClick = {
                    val firstSongImage = allSongs
                        .find { it.song.songId == viewModel.selectedSongIds.firstOrNull() }
                        ?.song?.imageUri

                    viewModel.saveDraftInfo(
                        name = playlistName,
                        imageUri = imageUri ?: firstSongImage,
                        tempImageUri = tempSelectedUri
                    )

                    Log.d("CreatePlaylistScreen", "Saved draft - Name: $playlistName, Image: $imageUri")
                    onNavToSelectSongs()
                },
                enabled = playlistName.isNotBlank()
            ) {
                Text("Next")
            }
        }
    ))

    // ✅ Đồng bộ tên playlist, nhưng KHÔNG reset ảnh
    LaunchedEffect(draft.name) {
        playlistName = draft.name
    }

    // ✅ Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                tempSelectedUri = uri
                imageUri = uri.toString()

                // ✅ Lưu ảnh vào draft ngay lập tức
                viewModel.saveDraftInfo(
                    name = playlistName,
                    imageUri = uri.toString(),
                    tempImageUri = uri
                )

                Log.d("CreatePlaylistScreen", "Selected image: $uri")
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ✅ Preview ảnh
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray)
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                val firstSongImage = allSongs
                    .find { it.song.songId == viewModel.selectedSongIds.firstOrNull() }
                    ?.song?.imageUri

                val displayImage = imageUri ?: firstSongImage

                if (displayImage != null) {
                    ThumbnailImage(
                        imageUri = displayImage,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = "Default image",
                        modifier = Modifier.size(90.dp),
                        tint = Color.Gray
                    )
                }
            }

            Text(
                text = "Tap to select image",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            OutlinedTextField(
                value = playlistName,
                onValueChange = {
                    playlistName = it
                    viewModel.updateDraftName(it)
                },
                label = { Text("Playlist Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Enter playlist name...") }
            )

            if (draft.selectedSongIds.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${draft.selectedSongIds.size} songs selected",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "You can add more songs in next step",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}
