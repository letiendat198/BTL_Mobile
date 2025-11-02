package com.ptit.btl_mobile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.ptit.btl_mobile.ui.components.Option

// TODO: PLEASE ONLY PUT STATE THAT NEED GLOBAL ACCESS HERE
class MainViewModel: ViewModel() {
    var showMenu by mutableStateOf(false)
    var menuOptions by mutableStateOf<List<Option>>(listOf())

    fun showMenuWithOptions(options: List<Option>) {
        menuOptions = options
        showMenu = true
    }
}