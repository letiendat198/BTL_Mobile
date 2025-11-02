package com.ptit.btl_mobile.ui.screens.library

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ptit.btl_mobile.R
import com.ptit.btl_mobile.ui.components.Option
import com.ptit.btl_mobile.ui.components.SongList
import com.ptit.btl_mobile.ui.components.TopAppBarContent

enum class LibraryTabs(val index: Int, val title: String) {
    SONG(0, "Song"),
    ARTIST(1, "Artist"),
    ALBUM(2, "Album")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onSetTopAppBar: (TopAppBarContent) -> Unit
) {
    // Using MainActivity store owner allow view model
    // to be maintained even after user navigated away
    // TODO: CAREFULL!! HERE WE ASSUME THIS COMPOSABLE IS BOUND TO AN ACTIVITY
    // IT SHOULD BE!
    val viewModel: LibraryViewModel = viewModel(viewModelStoreOwner = LocalActivity.current as ComponentActivity)
    Log.d("LIBRARY_SCREEN", "Recomposed when song list size is: " + viewModel.songs.size)

    var selectedTab by viewModel.selectedTab

    onSetTopAppBar(TopAppBarContent(
        title = "Library",
    ))
    Column {
        PrimaryTabRow(
            selectedTabIndex = selectedTab
        ) {
            enumValues<LibraryTabs>().forEach { libraryTabs ->
                Tab(
                    selected = selectedTab == libraryTabs.index,
                    onClick = {
                        selectedTab = libraryTabs.index
                    },
                    text = {
                        Text(libraryTabs.title)
                    }
                )
            }
        }

        when(selectedTab) {
            0 -> LibrarySongTab(viewModel)
        }
    }


}

@Composable
fun LibrarySongTab(viewModel: LibraryViewModel) {
    var searchQuery by viewModel.searchQuery
    // Persist song list state in Library view model
    viewModel.listState = viewModel.listState?:rememberLazyListState()

    val entryOptions = listOf<Option>(
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
                if (!searchQuery.isEmpty()) Icon(
                    painter = painterResource(R.drawable.close_small),
                    contentDescription = "Clear search",
                    modifier = Modifier.clickable {
                        searchQuery = ""
                        viewModel.filterSongName("")
                    }
                )
            },
            modifier = Modifier // To change height, you will need BasicTextField. Ain't gonna bother with that
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