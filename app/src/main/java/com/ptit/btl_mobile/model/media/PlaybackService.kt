package com.ptit.btl_mobile.model.media

import android.app.PendingIntent
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.ptit.btl_mobile.MainActivity

class PlaybackService: MediaSessionService() {
    private var mediaSession: MediaSession? = null

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this).build()
        mediaSession = MediaSession.Builder(this, player).build()
        val openAppIntent = Intent(this, MainActivity::class.java)
        mediaSession?.setSessionActivity(PendingIntent.getActivity(this, 0, openAppIntent,
            PendingIntent.FLAG_IMMUTABLE))
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

//    @OptIn(UnstableApi::class)
//    override fun onTaskRemoved(rootIntent: Intent?) {
//        pauseAllPlayersAndStopSelf()
//    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }
}