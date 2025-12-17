package com.ptit.btl_mobile.recommendation

import com.ptit.btl_mobile.model.database.*
import kotlin.math.sqrt

class RecommenderEngine(
    private val songs: List<Song>,
    private val songArtists: List<SongArtistCrossRef>,
    private val albums: List<Album>,
    private val playlistSongs: List<PlaylistSongCrossRef>
) {

    // INDEXING
    private val songIndex = songs.mapIndexed { i, s -> s.songId to i }.toMap()
    private val artistIndex = songArtists.map { it.artistId }.distinct()
        .mapIndexed { i, id -> id to i }.toMap()
    private val albumIndex = albums.map { it.albumId }
        .mapIndexed { i, id -> id to i }.toMap()
    private val playlistIndex = playlistSongs.map { it.playlistId }.distinct()
        .mapIndexed { i, id -> id to i }.toMap()

    private val numFeatures =
        artistIndex.size + albumIndex.size + playlistIndex.size

    // SONG FEATURE MATRIX
    private val songVectors: Array<IntArray> =
        Array(songs.size) { IntArray(numFeatures) }

    init {
        buildSongVectors()
    }

    private fun buildSongVectors() {
        // Artist features
        for (ref in songArtists) {
            val sIdx = songIndex[ref.songId] ?: continue
            val aIdx = artistIndex[ref.artistId] ?: continue
            songVectors[sIdx][aIdx] = 1
        }

        // Album features
        for (song in songs) {
            val albumId = song.songAlbumId ?: continue
            val sIdx = songIndex[song.songId] ?: continue
            val aIdx = albumIndex[albumId] ?: continue
            songVectors[sIdx][artistIndex.size + aIdx] = 1
        }

        // Playlist co-occurrence
        for (ref in playlistSongs) {
            val sIdx = songIndex[ref.songId] ?: continue
            val pIdx = playlistIndex[ref.playlistId] ?: continue
            songVectors[sIdx][artistIndex.size + albumIndex.size + pIdx] = 1
        }
    }

    // COSINE SIMILARITY
    private fun cosine(a: IntArray, b: IntArray): Double {
        var dot = 0.0
        var normA = 0.0
        var normB = 0.0

        for (i in a.indices) {
            dot += (a[i] * b[i])
            normA += (a[i] * a[i]).toDouble()
            normB += (b[i] * b[i]).toDouble()
        }
        return if (normA == 0.0 || normB == 0.0) 0.0
        else dot / (sqrt(normA) * sqrt(normB))
    }

    // SONG RECOMMENDATION
    fun recommendSongs(
        currentSongId: Long,
        topK: Int = 5
    ): List<Long> {

        val idx = songIndex[currentSongId] ?: return emptyList()
        val baseVector = songVectors[idx]

        return songs.mapIndexed { i, song ->
            song.songId to cosine(baseVector, songVectors[i])
        }
            .filter { it.first != currentSongId }
            .sortedByDescending { it.second }
            .take(topK)
            .map { it.first }
    }

    // PLAYLIST SIMILARITY
    fun recommendPlaylists(
        playlistId: Long,
        topK: Int = 3
    ): List<Long> {

        val playlistIds = playlistIndex.keys.toList()
        val vectors = buildPlaylistVectors()

        val idx = playlistIndex[playlistId] ?: return emptyList()
        val baseVector = vectors[idx]

        return playlistIds.mapIndexed { i, pid ->
            pid to cosine(baseVector, vectors[i])
        }
            .filter { it.first != playlistId }
            .sortedByDescending { it.second }
            .take(topK)
            .map { it.first }
    }

    // PLAYLIST VECTOR
    private fun buildPlaylistVectors(): Array<IntArray> {

        val size =
            songs.size + artistIndex.size + albumIndex.size

        val vectors = Array(playlistIndex.size) {
            IntArray(size)
        }

        for (ref in playlistSongs) {
            val pIdx = playlistIndex[ref.playlistId] ?: continue
            val sIdx = songIndex[ref.songId] ?: continue

            // Song
            vectors[pIdx][sIdx] = 1

            // Artist
            songArtists.filter { it.songId == ref.songId }
                .forEach {
                    val aIdx = artistIndex[it.artistId] ?: return@forEach
                    vectors[pIdx][songs.size + aIdx] = 1
                }

            // Album
            val albumId = songs.find { it.songId == ref.songId }?.songAlbumId
            if (albumId != null) {
                val alIdx = albumIndex[albumId]
                if (alIdx != null) {
                    vectors[pIdx][songs.size + artistIndex.size + alIdx] = 1
                }
            }

        }
        return vectors
    }
}
