package com.ptit.btl_mobile.model.media_utils

import android.util.Log
import com.kyant.taglib.TagLib
import com.ptit.btl_mobile.model.database.Artist

object MetadataHelper {
    fun readArtist(fd: Int): List<Artist>? {
        Log.d("METADATA", "Reading metadata from file")
        val metadata = TagLib.getMetadata(fd)!!
        Log.d("METADATA", metadata.propertyMap.keys.joinToString(", "))
        if (metadata.propertyMap.containsKey("ARTIST")) {
            val artistsName = metadata.propertyMap.get("ARTIST")
            val artists = mutableListOf<Artist>()

            return artistsName?.map { artist ->
                Log.d("METADATA", artist)
                Artist(
                    name = artist,
                )
            }?.toCollection(artists)


        }
        return null
    }
}