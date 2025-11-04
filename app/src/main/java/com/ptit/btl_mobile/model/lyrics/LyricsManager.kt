package com.ptit.btl_mobile.model.lyrics

import android.content.Context
import android.net.Uri
import java.io.File
import android.util.Log

class LyricsManager(private val context: Context) {

    private fun getLyricsFile(songId: Long): File {
        val lyricsDir = File(context.filesDir, "lyrics")
        if (!lyricsDir.exists()) lyricsDir.mkdirs()
        return File(lyricsDir, "$songId.lrc")
    }

    fun getLyrics(songId: Long): String? {
        return try {
            val file = getLyricsFile(songId)
            if (file.exists()) {
                val content = file.readText()
                Log.d("LyricsManager", "Read ${content.length} chars from $songId")
                content
            } else {
                Log.d("LyricsManager", "No lyrics file for $songId")
                null
            }
        } catch (e: Exception) {
            Log.e("LyricsManager", "Error reading lyrics for $songId", e)
            null
        }
    }

    fun saveLyrics(songId: Long, lyrics: String) {
        try {
            val file = getLyricsFile(songId)
            file.writeText(lyrics)
            Log.d("LyricsManager", "Saved ${lyrics.length} chars to $songId")
        } catch (e: Exception) {
            Log.e("LyricsManager", "Error saving lyrics for $songId", e)
        }
    }

    fun deleteLyrics(songId: Long) {
        try {
            val file = getLyricsFile(songId)
            if (file.exists()) {
                file.delete()
                Log.d("LyricsManager", "Deleted lyrics for $songId")
            }
        } catch (e: Exception) {
            Log.e("LyricsManager", "Error deleting lyrics for $songId", e)
        }
    }

    fun importFromLrcFile(songId: Long, uri: Uri): String? {
        return try {
            val content = context.contentResolver.openInputStream(uri)?.use {
                it.bufferedReader().readText()
            }
            if (content != null) {
                Log.d("LyricsManager", "Imported ${content.length} chars")
                // Giới hạn size để tránh đơ
                if (content.length > 50000) {
                    Log.w("LyricsManager", "File too large, truncating")
                    val truncated = content.take(50000)
                    saveLyrics(songId, truncated)
                    truncated
                } else {
                    saveLyrics(songId, content)
                    content
                }
            } else {
                Log.e("LyricsManager", "Cannot read content from URI")
                null
            }
        } catch (e: Exception) {
            Log.e("LyricsManager", "Error importing lyrics", e)
            null
        }
    }

    fun clearAllLyrics() {
        val lyricsDir = File(context.filesDir, "lyrics")
        if (lyricsDir.exists()) {
            lyricsDir.listFiles()?.forEach { it.delete() }
        }
    }
}