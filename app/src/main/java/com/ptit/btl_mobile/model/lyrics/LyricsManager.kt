package com.ptit.btl_mobile.model.lyrics

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File

class LyricsManager(private val context: Context) {

    fun saveLyricsToFile(songId: Long, lyrics: String): String? {
        return try {
            val lyricsDir = File(context.filesDir, "lyrics")
            if (!lyricsDir.exists()) lyricsDir.mkdirs()

            val file = File(lyricsDir, "$songId.lrc")
            file.writeText(lyrics)

            val uri = Uri.fromFile(file).toString()
            Log.d("LyricsManager", "Saved lyrics to: $uri")
            uri
        } catch (e: Exception) {
            Log.e("LyricsManager", "Error saving lyrics for $songId", e)
            null
        }
    }

    fun getLyricsFromUri(lyricUri: String?): String? {
        if (lyricUri == null) return null

        return try {
            val uri = Uri.parse(lyricUri)
            val file = File(uri.path ?: return null)

            if (file.exists()) {
                val content = file.readText()
                Log.d("LyricsManager", "Read ${content.length} chars from $lyricUri")
                content
            } else {
                Log.d("LyricsManager", "Lyrics file not found: $lyricUri")
                null
            }
        } catch (e: Exception) {
            Log.e("LyricsManager", "Error reading lyrics from $lyricUri", e)
            null
        }
    }

    fun importLrcFile(songId: Long, uri: Uri): String? {
        return try {
            val content = context.contentResolver.openInputStream(uri)?.use {
                it.bufferedReader().readText()
            }

            if (content != null) {
                Log.d("LyricsManager", "Imported ${content.length} chars")
                if (content.length > 50000) {
                    Log.w("LyricsManager", "File too large, truncating")
                    val truncated = content.take(50000)
                    saveLyricsToFile(songId, truncated)
                } else {
                    saveLyricsToFile(songId, content)
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

    fun deleteLyricsFile(lyricUri: String?) {
        if (lyricUri == null) return

        try {
            val uri = Uri.parse(lyricUri)
            val file = File(uri.path ?: return)
            if (file.exists()) {
                file.delete()
                Log.d("LyricsManager", "Deleted lyrics file: $lyricUri")
            }
        } catch (e: Exception) {
            Log.e("LyricsManager", "Error deleting lyrics file", e)
        }
    }

    fun clearAllLyrics() {
        val lyricsDir = File(context.filesDir, "lyrics")
        if (lyricsDir.exists()) {
            lyricsDir.listFiles()?.forEach { it.delete() }
        }
    }
}