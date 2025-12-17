package com.ptit.btl_mobile.ui.screens.party

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel

enum class PartyState {
    DEFAULT,
    JOIN,
    JOINED,
    HOST
}

class ListeningPartyViewModel: ViewModel() {
    var currentPartyState = mutableStateOf<PartyState>(PartyState.DEFAULT)

    fun changePartyState(state: PartyState) {
        currentPartyState.value = state
    }
}