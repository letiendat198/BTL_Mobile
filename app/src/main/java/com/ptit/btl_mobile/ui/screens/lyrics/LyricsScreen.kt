package com.ptit.btl_mobile.ui.screens.lyrics

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ptit.btl_mobile.model.lyrics.LrcLine
import com.ptit.btl_mobile.ui.screens.player.PlayerViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsScreen(
    songId: Long,
    songTitle: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = LocalActivity.current as ComponentActivity

    val viewModel: LyricsViewModel = viewModel(
        factory = LyricsViewModelFactory(context, songId),
        key = "lyrics_$songId"
    )

    val playerViewModel: PlayerViewModel = viewModel(viewModelStoreOwner = activity)
    val currentPosition by playerViewModel.currentPosition

    val lyrics by viewModel.lyrics.collectAsState()
    val lrcLines by viewModel.lrcLines.collectAsState()
    val currentLineIndex by viewModel.currentLineIndex.collectAsState()
    val isSynced by viewModel.isSynced.collectAsState()
    val isEditing by viewModel.isEditing.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var editText by remember { mutableStateOf("") }

    // Update current position
    LaunchedEffect(currentPosition) {
        viewModel.updateCurrentPosition(currentPosition * 1000)
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.importLrcFile(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lyrics - $songTitle", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (lyrics != null && !isEditing) {
                        IconButton(onClick = {
                            editText = lyrics ?: ""
                            viewModel.startEditing()
                        }) {
                            Icon(Icons.Default.Edit, "Edit")
                        }
                        IconButton(onClick = { viewModel.deleteLyrics() }) {
                            Icon(Icons.Default.Delete, "Delete")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                isEditing -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        OutlinedTextField(
                            value = editText,
                            onValueChange = { editText = it },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            placeholder = { Text("Enter lyrics here...") },
                            maxLines = Int.MAX_VALUE
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.cancelEditing() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel")
                            }
                            Button(
                                onClick = { viewModel.saveLyrics(editText) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Save")
                            }
                        }
                    }
                }

                lyrics != null -> {
                    if (isSynced) {
                        SyncedLyricsView(lrcLines, currentLineIndex)
                    } else {
                        PlainLyricsView(lyrics!!)
                    }
                }

                else -> {
                    EmptyLyricsView(filePickerLauncher) {
                        editText = ""
                        viewModel.startEditing()
                    }
                }
            }
        }
    }
}

@Composable
fun SyncedLyricsView(lines: List<LrcLine>, currentIndex: Int) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto scroll
    LaunchedEffect(currentIndex) {
        if (currentIndex in lines.indices) {
            coroutineScope.launch {
                listState.animateScrollToItem(
                    index = maxOf(0, currentIndex - 2)
                )
            }
        }
    }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(lines) { index, line ->
            val isActive = index == currentIndex

            Text(
                text = line.text,
                fontSize = if (isActive) 20.sp else 16.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                color = if (isActive)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
        }
    }
}

@Composable
fun PlainLyricsView(lyrics: String) {
    Text(
        text = lyrics,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        style = MaterialTheme.typography.bodyLarge
    )
}

@Composable
fun EmptyLyricsView(
    filePickerLauncher: ActivityResultLauncher<String>,
    onAddManually: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No lyrics available",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { filePickerLauncher.launch("*/*") }
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Import .lrc file")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(onClick = onAddManually) {
            Icon(Icons.Default.Edit, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add lyrics manually")
        }
    }
}