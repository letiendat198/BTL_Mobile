package com.ptit.btl_mobile.ui.screens.library

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ptit.btl_mobile.R
import com.ptit.btl_mobile.ui.components.Option
import com.ptit.btl_mobile.ui.components.SongList
import com.ptit.btl_mobile.ui.components.TopAppBarContent
import com.ptit.btl_mobile.ui.screens.library.tabs.AlbumsTab
import com.ptit.btl_mobile.ui.screens.library.tabs.ArtistsTab

enum class LibraryTabs(val index: Int, val title: String) {
    SONG(0, "Songs"),
    ALBUM(1, "Albums"),
    ARTIST(2, "Artists")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    navController: NavController = rememberNavController(),
    onSetTopAppBar: (TopAppBarContent) -> Unit = {}
) {
    // Using MainActivity store owner allow view model
    // to be maintained even after user navigated away
    val viewModel: LibraryViewModel = viewModel(
        viewModelStoreOwner = LocalActivity.current as ComponentActivity
    )
    Log.d("LIBRARY_SCREEN", "Recomposed when song list size is: " + viewModel.songs.size)

    var selectedTab by viewModel.selectedTab

    onSetTopAppBar(TopAppBarContent(
        title = "Library",
        actions = {
            IconButton(
                onClick = {
                    viewModel.sortSong() // TODO: TEMP
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.sort),
                    contentDescription = "Sort"
                )
            }
        }
    ))

    Column {
        PrimaryTabRow(
            selectedTabIndex = selectedTab
        ) {
            enumValues<LibraryTabs>().forEach { libraryTab ->
                Tab(
                    selected = selectedTab == libraryTab.index,
                    onClick = {
                        selectedTab = libraryTab.index
                    },
                    text = {
                        Text(libraryTab.title)
                    }
                )
            }
        }

        when(selectedTab) {
            0 -> LibrarySongTab(viewModel)
            1 -> AlbumsTab(
                navController = navController,
                viewModel = viewModel
            )
            2 -> ArtistsTab(
                navController = navController,
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun LibrarySongTab(viewModel: LibraryViewModel) {
    var searchQuery by viewModel.searchQuery
    // Persist song list state in Library view model
    viewModel.listState = viewModel.listState ?: rememberLazyListState()

    val entryOptions = listOf(
        Option(
            title = "Delete song from device"
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        TextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                viewModel.filterSongName(it)
            },
            maxLines = 1,
            placeholder = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.search),
                        contentDescription = "Search icon"
                    )
                    Text("Search library")
                }
            },
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    Icon(
                        painter = painterResource(R.drawable.close_small),
                        contentDescription = "Clear search",
                        modifier = Modifier.clickable {
                            searchQuery = ""
                            viewModel.filterSongName("")
                        }
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp, 10.dp)
                .clip(shape = RoundedCornerShape(10.dp))
        )

        SongList(
            songs = viewModel.songs,
            customState = viewModel.listState,
            entryOptions = entryOptions
        )
    }
}