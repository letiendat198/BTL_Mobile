package com.ptit.btl_mobile.ui.screens.party

import android.app.Application
import android.content.Context
import android.media.session.MediaSessionManager
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.ptit.btl_mobile.model.media.MediaControllerStore
import com.ptit.btl_mobile.model.media.MediaLoader
import com.ptit.btl_mobile.model.party.server.Server
import com.ptit.btl_mobile.model.party.utils.HostInfo
import com.ptit.btl_mobile.ui.screens.player.PlayerViewModel
import com.ptit.btl_mobile.ui.screens.playlist.PlaylistViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HostViewModel(val context: Application): AndroidViewModel(context) {
    val server = Server(context)
    var hostIp = mutableStateOf("0.0.0.0")

    // Listen to media controller events and pushes information to clients
    inner class MediaControlCallback: Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)

            // TODO: Handler this
        }

        @OptIn(UnstableApi::class)
        override fun onPositionDiscontinuity(oldPosition: Player.PositionInfo, newPosition: Player.PositionInfo, reason: Int) {
            super.onPositionDiscontinuity(oldPosition, newPosition, reason)
            Log.d("HOST_VM", "Position discontinuity called, uri: " + newPosition.mediaItem?.localConfiguration?.uri)

            val uri = newPosition.mediaItem?.localConfiguration?.uri
            if (uri == null) return
            viewModelScope.launch(Dispatchers.IO) {
                server.syncAll(uri, newPosition.positionMs)
            }

        }

//        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
//            Log.d("HOST_VM", "Item transition called, uri: " + mediaItem?.localConfiguration?.uri)
//            super.onMediaItemTransition(mediaItem, reason)
//            val uri = mediaItem?.localConfiguration?.uri
//            if (uri == null) return
//            viewModelScope.launch(Dispatchers.IO) {
//                server.syncAll(uri, 0)
//            }
            // Position discontinuity will be called on the same cases anyway
            // Also, this somehow get called with the first track when you first choose a song?
            // So just use onPositionDiscontinuity
//        }
    }

    var clientList = mutableStateListOf<String>()

    init {
        Log.d("HOST_VM", "Init")
        hostIp.value = HostInfo.getHostIP(context)

        server.setOnChangeCallback { clientMap ->
            clientList.clear()
            clientList.addAll(clientMap.keys)
        }
        server.listen()

        MediaControllerStore.mediaController?.addListener(MediaControlCallback())
    }
}