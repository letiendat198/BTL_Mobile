package com.ptit.btl_mobile.ui.screens.editmetadata

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ptit.btl_mobile.R
import com.ptit.btl_mobile.model.database.Artist
import com.ptit.btl_mobile.ui.components.ThumbnailImage
import com.ptit.btl_mobile.ui.components.TopAppBarContent

@Composable
fun EditMetadataScreen(
    songId: Long,
    onBack: () -> Unit = {},
    onSetTopAppBar: (TopAppBarContent) -> Unit
) {
    val viewModel: EditMetadataViewModel = viewModel(
        viewModelStoreOwner = LocalActivity.current as ComponentActivity
    )

    var songNameInput by remember { mutableStateOf("") }
    var artistNameInput by remember { mutableStateOf("") }
    var albumNameInput by remember { mutableStateOf("") }
    var genreInput by remember { mutableStateOf("") }
    var thumbnailUri by remember { mutableStateOf<String?>(null) }

    // Already remembered internally?
    val songInfo by viewModel.getSongFullInfo(songId).collectAsStateWithLifecycle(null)

    LaunchedEffect(songInfo) {
        songNameInput = songInfo?.song?.name?:""
        artistNameInput = songInfo?.artists?.joinToString(", ") {it.name}?:""
        albumNameInput = songInfo?.album?.name?:""
        genreInput = songInfo?.album?.genre?:""
        thumbnailUri = songInfo?.song?.imageUri
    }

    onSetTopAppBar(TopAppBarContent(
        title = "Edit metadata",
        navigationIcon = {
            IconButton(onClick = {onBack()}) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            IconButton(onClick = {
                songInfo?.let {viewModel.saveSongMetadata(it.copy(
                    song = it.song.copy(name = songNameInput),
                    artists = artistNameInput.split(", ").map { s -> Artist(name = s) },
                    album = it.album.copy(name = albumNameInput, genre = genreInput)
                )) }
            }) {
                Icon(painter = painterResource(R.drawable.save), contentDescription = "Save")
            }
        }
    ))

    if (songInfo != null) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .padding(10.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Text("Tap the thumbnail image to change it", fontWeight = FontWeight.Light)
            ThumbnailImage(
                imageUri = thumbnailUri,
                modifier = Modifier.height(250.dp).aspectRatio(1f)
            )
            OutlinedTextField(
                value = songNameInput,
                singleLine = true,
                onValueChange = { songNameInput = it },
                label = { Text("Song name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = artistNameInput,
                singleLine = true,
                onValueChange = { artistNameInput = it },
                label = { Text("Artists name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = albumNameInput,
                singleLine = true,
                onValueChange = { albumNameInput = it },
                label = { Text("Album name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = genreInput,
                singleLine = true,
                onValueChange = { genreInput = it },
                label = { Text("Genre") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}