package com.ptit.btl_mobile.ui.screens.lyrics

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class LyricsViewModelFactory(
    private val context: Context,
    private val songId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LyricsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LyricsViewModel(context, songId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}