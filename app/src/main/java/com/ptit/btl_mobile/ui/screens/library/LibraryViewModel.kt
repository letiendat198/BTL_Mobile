package com.ptit.btl_mobile.ui.screens.library

import android.util.Log
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptit.btl_mobile.model.database.*
import kotlinx.coroutines.launch

// Wrapper classes trong ViewModel
data class AlbumWithInfo(
    val album: Album,
    val artistName: String?,
    val songCount: Int
)

data class ArtistWithInfo(
    val artist: Artist,
    val albumCount: Int,
    val songCount: Int
)

class LibraryViewModel: ViewModel() {
    // ===== CODE CŨ - GIỮ NGUYÊN =====
    private var _songs = listOf<SongWithArtists>()
    var songs by mutableStateOf(_songs)
    var searchQuery = mutableStateOf("")
    var selectedTab = mutableIntStateOf(0);
    var listState: LazyListState? = null

    // ===== ALBUMS =====
    private var _albumsWithInfo = listOf<AlbumWithInfo>()
    var albumsWithInfo by mutableStateOf(_albumsWithInfo)

    // ===== ARTISTS =====
    private var _artistsWithInfo = listOf<ArtistWithInfo>()
    var artistsWithInfo by mutableStateOf(_artistsWithInfo)

    // ===== DETAIL SCREENS =====
    private var _selectedAlbum: Album? = null
    var selectedAlbum by mutableStateOf(_selectedAlbum)

    private var _selectedAlbumArtistName: String? = null
    var selectedAlbumArtistName by mutableStateOf(_selectedAlbumArtistName)

    private var _selectedAlbumSongCount: Int = 0
    var selectedAlbumSongCount by mutableStateOf(_selectedAlbumSongCount)

    private var _albumSongs = listOf<SongWithArtists>()
    var albumSongs by mutableStateOf(_albumSongs)

    private var _selectedArtist: Artist? = null
    var selectedArtist by mutableStateOf(_selectedArtist)

    private var _selectedArtistAlbumCount: Int = 0
    var selectedArtistAlbumCount by mutableStateOf(_selectedArtistAlbumCount)

    private var _selectedArtistSongCount: Int = 0
    var selectedArtistSongCount by mutableStateOf(_selectedArtistSongCount)

    private var _artistSongs = listOf<SongWithArtists>()
    var artistSongs by mutableStateOf(_artistSongs)

    private var _artistAlbums = listOf<AlbumWithInfo>()
    var artistAlbums by mutableStateOf(_artistAlbums)

    init {
        Log.d("LIBRARY_VIEW_MODEL", "ViewModel is being constructed. This should only happens once")
        getAllSongs()
        getAllAlbums()
        getAllArtists()
    }

    fun getAllSongs() {
        val db = Database.getInstance()
        viewModelScope.launch {
            _songs = db.SongDAO().getAllWithArtists()
            songs = _songs
        }
    }

    fun getAllAlbums() {
        val db = Database.getInstance()
        viewModelScope.launch {
            val albums = db.AlbumDAO().getAll()

            // Load thêm thông tin cho mỗi album
            _albumsWithInfo = albums.map { album ->
                val artistName = db.AlbumDAO().getFirstArtistNameByAlbumId(album.albumId)
                val songCount = db.AlbumDAO().getSongCountByAlbumId(album.albumId)
                AlbumWithInfo(album, artistName, songCount)
            }
            albumsWithInfo = _albumsWithInfo

            Log.d("LIBRARY_VIEW_MODEL", "Loaded ${albumsWithInfo.size} albums")
        }
    }

    fun getAllArtists() {
        val db = Database.getInstance()
        viewModelScope.launch {
            val artists = db.ArtistDAO().getAll()

            // Load thêm thông tin cho mỗi artist
            _artistsWithInfo = artists.map { artist ->
                val albumCount = db.ArtistDAO().getAlbumCountByArtistId(artist.artistId)
                val songCount = db.ArtistDAO().getSongCountByArtistId(artist.artistId)
                ArtistWithInfo(artist, albumCount, songCount)
            }
            artistsWithInfo = _artistsWithInfo

            Log.d("LIBRARY_VIEW_MODEL", "Loaded ${artistsWithInfo.size} artists")
        }
    }

    fun loadAlbumDetail(albumId: Long) {
        val db = Database.getInstance()
        viewModelScope.launch {
            _selectedAlbum = db.AlbumDAO().getById(albumId)
            selectedAlbum = _selectedAlbum

            _selectedAlbumArtistName = db.AlbumDAO().getFirstArtistNameByAlbumId(albumId)
            selectedAlbumArtistName = _selectedAlbumArtistName

            _albumSongs = db.AlbumDAO().getSongsByAlbumId(albumId)
            albumSongs = _albumSongs

            _selectedAlbumSongCount = albumSongs.size
            selectedAlbumSongCount = _selectedAlbumSongCount

            Log.d("LIBRARY_VIEW_MODEL", "Loaded album: ${selectedAlbum?.name} with ${albumSongs.size} songs")
        }
    }

    fun loadArtistDetail(artistId: Long) {
        val db = Database.getInstance()
        viewModelScope.launch {
            _selectedArtist = db.ArtistDAO().getById(artistId)
            selectedArtist = _selectedArtist

            _selectedArtistAlbumCount = db.ArtistDAO().getAlbumCountByArtistId(artistId)
            selectedArtistAlbumCount = _selectedArtistAlbumCount

            _selectedArtistSongCount = db.ArtistDAO().getSongCountByArtistId(artistId)
            selectedArtistSongCount = _selectedArtistSongCount

            _artistSongs = db.ArtistDAO().getSongsByArtistId(artistId)
            artistSongs = _artistSongs

            // Load albums của artist với info
            val albums = db.AlbumDAO().getByArtistId(artistId)
            _artistAlbums = albums.map { album ->
                val artistName = db.AlbumDAO().getFirstArtistNameByAlbumId(album.albumId)
                val songCount = db.AlbumDAO().getSongCountByAlbumId(album.albumId)
                AlbumWithInfo(album, artistName, songCount)
            }
            artistAlbums = _artistAlbums

            Log.d("LIBRARY_VIEW_MODEL", "Loaded artist: ${selectedArtist?.name}")
        }
    }

    fun filterSongName(query: String) {
        if (query.isEmpty()) {
            songs = _songs
            return
        }

        songs = _songs.filter { (song, artists) ->
            song.name.contains(query, true) ||
                    artists.any {artist -> artist.name.contains(query, true)} }
    }

    fun sortSong() {
        songs = _songs.sortedBy { (song, artists) -> song.name }
    }
}