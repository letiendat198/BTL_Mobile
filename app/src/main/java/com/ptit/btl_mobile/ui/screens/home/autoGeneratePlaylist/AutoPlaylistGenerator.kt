package com.ptit.btl_mobile.ui.screens.home.autoGeneratePlaylist

import com.ptit.btl_mobile.model.database.SongWithArtists

/**
 * Generator for automatic playlist creation
 * Provides various algorithms to generate playlists based on different criteria
 */
object AutoPlaylistGenerator {

    // ============================================
    // TIME-BASED GENERATORS
    // ============================================

    /**
     * Generate playlist from recently added songs
     * @param allSongs List of all available songs
     * @param limit Maximum number of songs to include
     * @return List of songs sorted by date added (newest first)
     */
    fun generateRecentlyAdded(
        allSongs: List<SongWithArtists>,
        limit: Int = 20
    ): List<SongWithArtists> {
        return allSongs
            .sortedByDescending { it.song.dateAdded }
            .take(limit)
    }

    /**
     * Generate playlist from songs added within the last X days
     * @param allSongs List of all available songs
     * @param days Number of days to look back
     * @return List of songs added within the specified timeframe
     */
    fun generateRecentDays(
        allSongs: List<SongWithArtists>,
        days: Int = 7
    ): List<SongWithArtists> {
        val cutoffTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        return allSongs
            .filter { it.song.dateAdded >= cutoffTime }
            .sortedByDescending { it.song.dateAdded }
    }

    /**
     * Generate playlist from oldest songs in library
     * @param allSongs List of all available songs
     * @param limit Maximum number of songs to include
     * @return List of oldest songs
     */
    fun generateOldestSongs(
        allSongs: List<SongWithArtists>,
        limit: Int = 25
    ): List<SongWithArtists> {
        return allSongs
            .sortedBy { it.song.dateAdded }
            .take(limit)
    }

    // ============================================
    // DURATION-BASED GENERATORS
    // ============================================

    /**
     * Generate playlist with short songs (ideal for workouts)
     * @param allSongs List of all available songs
     * @param maxDuration Maximum song duration in milliseconds (default: 3 minutes)
     * @return List of short songs sorted by duration
     */
    fun generateShortSongs(
        allSongs: List<SongWithArtists>,
        maxDuration: Long = 180_000L // 3 minutes
    ): List<SongWithArtists> {
        return allSongs
            .filter { it.song.duration <= maxDuration }
            .sortedBy { it.song.duration }
    }

    /**
     * Generate playlist with long songs (ideal for relaxation)
     * @param allSongs List of all available songs
     * @param minDuration Minimum song duration in milliseconds (default: 4 minutes)
     * @return List of long songs sorted by duration (longest first)
     */
    fun generateLongSongs(
        allSongs: List<SongWithArtists>,
        minDuration: Long = 240_000L // 4 minutes
    ): List<SongWithArtists> {
        return allSongs
            .filter { it.song.duration >= minDuration }
            .sortedByDescending { it.song.duration }
    }

    /**
     * Generate playlist matching a target total duration
     * Useful for commutes or specific time windows
     * @param allSongs List of all available songs
     * @param targetDurationMs Target total duration in milliseconds
     * @return List of songs that approximately match target duration
     */
    fun generateByTargetDuration(
        allSongs: List<SongWithArtists>,
        targetDurationMs: Long
    ): List<SongWithArtists> {
        val TOLERANCE_MS = 300_000L // 5 minutes tolerance
        val shuffled = allSongs.shuffled()
        val result = mutableListOf<SongWithArtists>()
        var currentDuration = 0L

        for (song in shuffled) {
            if (currentDuration + song.song.duration <= targetDurationMs + TOLERANCE_MS) {
                result.add(song)
                currentDuration += song.song.duration
            }
            if (currentDuration >= targetDurationMs) break
        }

        return result
    }

    // ============================================
    // RANDOM & SORTING GENERATORS
    // ============================================

    /**
     * Generate quick random mix
     * Size adapts based on library size
     * @param allSongs List of all available songs
     * @return Shuffled list with adaptive size
     */
    fun generateQuickMix(
        allSongs: List<SongWithArtists>
    ): List<SongWithArtists> {
        val count = when {
            allSongs.size < 10 -> allSongs.size
            allSongs.size < 30 -> 15
            else -> 30
        }
        return allSongs.shuffled().take(count)
    }

    /**
     * Generate random playlist with specific size
     * @param allSongs List of all available songs
     * @param count Number of songs to include
     * @return Shuffled list of specified size
     */
    fun generateRandomMix(
        allSongs: List<SongWithArtists>,
        count: Int = 25
    ): List<SongWithArtists> {
        return if (allSongs.size <= count) {
            allSongs.shuffled()
        } else {
            allSongs.shuffled().take(count)
        }
    }

    /**
     * Generate playlist sorted alphabetically by song name
     * @param allSongs List of all available songs
     * @return Alphabetically sorted list
     */
    fun generateAlphabetical(
        allSongs: List<SongWithArtists>
    ): List<SongWithArtists> {
        return allSongs.sortedBy { it.song.name.lowercase() }
    }

    /**
     * Generate playlist from songs starting with a specific letter
     * @param allSongs List of all available songs
     * @param letter Target first letter
     * @return Sorted list of songs starting with the specified letter
     */
    fun generateByFirstLetter(
        allSongs: List<SongWithArtists>,
        letter: Char
    ): List<SongWithArtists> {
        return allSongs
            .filter { it.song.name.firstOrNull()?.uppercaseChar() == letter.uppercaseChar() }
            .sortedBy { it.song.name }
    }

    // ============================================
    // ARTIST-BASED GENERATORS
    // ============================================

    /**
     * Generate playlist from a specific artist
     * @param allSongs List of all available songs
     * @param artistName Target artist name (case-insensitive)
     * @return List of songs by the specified artist
     */
    fun generateByArtist(
        allSongs: List<SongWithArtists>,
        artistName: String
    ): List<SongWithArtists> {
        return allSongs.filter { song ->
            song.artists.any { it.name.equals(artistName, ignoreCase = true) }
        }
    }

    /**
     * Generate "Best of Artists" playlist
     * Takes top N songs from each artist and shuffles them
     * @param allSongs List of all available songs
     * @param songsPerArtist Number of songs to take from each artist
     * @return Shuffled mix of top songs from each artist
     */
    fun generateBestOfArtists(
        allSongs: List<SongWithArtists>,
        songsPerArtist: Int = 3
    ): List<SongWithArtists> {
        val songsByArtist = allSongs.groupBy { song ->
            song.artists.firstOrNull()?.name ?: "Unknown"
        }

        return songsByArtist
            .flatMap { (_, songs) -> songs.shuffled().take(songsPerArtist) }
            .shuffled()
    }

        // ============================================
        // HELPER CONSTANTS
        // ============================================

    object DurationConstants {
        const val ONE_MINUTE_MS = 60_000L
        const val THREE_MINUTES_MS = 180_000L
        const val FOUR_MINUTES_MS = 240_000L
        const val THIRTY_MINUTES_MS = 1_800_000L
        const val ONE_HOUR_MS = 3_600_000L
    }
}

/**
 * Represents a playlist generation template
 * @property id Unique identifier for the template
 * @property name Display name
 * @property description Brief description of what the template does
 * @property icon Emoji icon for visual representation
 * @property generator Function that generates the playlist
 */
data class PlaylistTemplate(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val generator: (List<SongWithArtists>) -> List<SongWithArtists>
)

/**
 * Registry of all available playlist templates
 * Centralized location for template definitions
 */
object PlaylistTemplates {

    /**
     * Get all available playlist templates
     * @return List of all predefined templates
     */
    fun getAll(): List<PlaylistTemplate> = listOf(
        // Quick & Random
        createTemplate(
            id = "quick_mix",
            name = "Quick Mix",
            description = "Random mix of your songs",
            icon = "🎲",
            generator = { AutoPlaylistGenerator.generateQuickMix(it) }
        ),
        createTemplate(
            id = "random_25",
            name = "Random 25",
            description = "25 random songs \n",
            icon = "🎵",
            generator = { AutoPlaylistGenerator.generateRandomMix(it, 25) }
        ),

        // Time-based
        createTemplate(
            id = "recently_added",
            name = "Recently Added",
            description = "Your newest songs \n",
            icon = "🆕",
            generator = { AutoPlaylistGenerator.generateRecentlyAdded(it, 25) }
        ),
        createTemplate(
            id = "last_7_days",
            name = "Last 7 Days",
            description = "Songs added in last week",
            icon = "📅",
            generator = { AutoPlaylistGenerator.generateRecentDays(it, 7) }
        ),
        createTemplate(
            id = "oldest_songs",
            name = "Memory Lane",
            description = "Your oldest songs",
            icon = "⏳",
            generator = { AutoPlaylistGenerator.generateOldestSongs(it) }
        ),

        // Duration-based
        createTemplate(
            id = "workout",
            name = "Workout Mix",
            description = "Short, energetic songs",
            icon = "💪",
            generator = { AutoPlaylistGenerator.generateShortSongs(it) }
        ),
        createTemplate(
            id = "chill",
            name = "Chill Vibes",
            description = "Longer, relaxing songs",
            icon = "😌",
            generator = { AutoPlaylistGenerator.generateLongSongs(it) }
        ),
        createTemplate(
            id = "commute_30",
            name = "30 Min Commute",
            description = "Perfect for your daily commute",
            icon = "🚗",
            generator = {
                AutoPlaylistGenerator.generateByTargetDuration(
                    it,
                    AutoPlaylistGenerator.DurationConstants.THIRTY_MINUTES_MS
                )
            }
        ),
        createTemplate(
            id = "hour_long",
            name = "1 Hour Mix",
            description = "About 60 minutes of music",
            icon = "⏰",
            generator = {
                AutoPlaylistGenerator.generateByTargetDuration(
                    it,
                    AutoPlaylistGenerator.DurationConstants.ONE_HOUR_MS
                )
            }
        ),

        // Sorting & Organization
        createTemplate(
            id = "alphabetical",
            name = "A to Z",
            description = "All songs alphabetically",
            icon = "🔤",
            generator = { AutoPlaylistGenerator.generateAlphabetical(it) }
        ),
        createTemplate(
            id = "best_of_artists",
            name = "Best of Artists",
            description = "Top songs from each artist",
            icon = "⭐",
            generator = { AutoPlaylistGenerator.generateBestOfArtists(it) }
        )
    )

    /**
     * Helper function to create a template with consistent structure
     */
    private fun createTemplate(
        id: String,
        name: String,
        description: String,
        icon: String,
        generator: (List<SongWithArtists>) -> List<SongWithArtists>
    ) = PlaylistTemplate(id, name, description, icon, generator)
}