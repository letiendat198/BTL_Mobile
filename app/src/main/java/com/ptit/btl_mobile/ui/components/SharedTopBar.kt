package com.ptit.btl_mobile.ui.components

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

data class TopAppBarContent(
    val title: String,
    val navigationIcon: @Composable () -> Unit = {},
    val actions: @Composable (RowScope) -> Unit = {}
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedTopBar(content: TopAppBarContent) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    TopAppBar(
        title = { Text(content.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        navigationIcon = content.navigationIcon,
        actions = content.actions,
        scrollBehavior = scrollBehavior,
        modifier = Modifier.background(Color.Cyan)
    )
}