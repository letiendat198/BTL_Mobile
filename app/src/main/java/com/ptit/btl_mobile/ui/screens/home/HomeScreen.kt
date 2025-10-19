package com.ptit.btl_mobile.ui.screens.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.ptit.btl_mobile.R
import com.ptit.btl_mobile.ui.theme.BTL_MobileTheme

@Composable
fun HomeScreen() {
    Text("Hello")
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BTL_MobileTheme {
        HomeScreen()
    }
}