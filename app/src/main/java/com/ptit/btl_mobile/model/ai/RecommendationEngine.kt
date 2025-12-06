package com.ptit.btl_mobile.model.ai

import android.content.Context
import android.util.Log
import com.ptit.btl_mobile.model.database.Song
import com.ptit.btl_mobile.model.database.SongWithArtists
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.abs

class RecommendationEngine(private val context: Context) {

    private var tfliteInterpreter: Interpreter? = null
    private val MODEL_FILENAME = "recommendation_model.tflite"

    init {
        try {
            val model = loadModelFile()
            if (model != null) {
                tfliteInterpreter = Interpreter(model)
                Log.d("AI_ENGINE", "Model loaded successfully")
            }
        } catch (e: Exception) {
            Log.e("AI_ENGINE", "Error loading model: ${e.message}. Using fallback logic.")
        }
    }

    private fun loadModelFile(): MappedByteBuffer? {
        return try {
            val fileDescriptor = context.assets.openFd(MODEL_FILENAME)
            val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val startOffset = fileDescriptor.startOffset
            val declaredLength = fileDescriptor.declaredLength
            fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        } catch (e: Exception) {
            null
        }
    }

    fun getRecommendations(seedSong: SongWithArtists, allSongs: List<SongWithArtists>, limit: Int = 10): List<Song> {
        if (tfliteInterpreter != null) {
            // Nếu có model TFLite, ưu tiên chạy model
            return runAIInference(seedSong, allSongs, limit)
        } else {
            Log.w("RECOMMEND_ENGINE", "USING FALLBACK ALGORITHM")
            // Nếu không, chạy thuật toán logic dự phòng
            return runHeuristicFallback(seedSong, allSongs, limit)
        }
    }

    private fun runAIInference(seedSong: SongWithArtists, allSongs: List<SongWithArtists>, limit: Int): List<Song> {
        val recommendations = allSongs.map { songWithArtists ->
            // Chuẩn bị input (ví dụ: duration) - bạn có thể tùy chỉnh để khớp với model
            val input = floatArrayOf(
                seedSong.song.duration.toFloat(),
                songWithArtists.song.duration.toFloat()
            )
            val output = Array(1) { FloatArray(1) }

            try {
                tfliteInterpreter?.run(input, output)
                val score = output[0][0]
                songWithArtists.song to score // Trả về đối tượng Song và điểm số
            } catch (e: Exception) {
                songWithArtists.song to 0f // Gặp lỗi thì cho điểm 0
            }
        }

        return recommendations.sortedByDescending { it.second }
            .map { it.first }
            .take(limit)
    }

    private fun runHeuristicFallback(seedSong: SongWithArtists, allSongs: List<SongWithArtists>, limit: Int): List<Song> {
        // Lấy danh sách ID nghệ sĩ của bài hát gốc
        val seedArtistIds = seedSong.artists.map { it.artistId }.toSet()

        return allSongs.asSequence()
            .filter { it.song.songId != seedSong.song.songId } // 1. Loại bỏ bài hát gốc
            .map { songWithArtists ->
                var score = 0.0

                // 2. Cùng nghệ sĩ (+50 điểm)
                val currentArtistIds = songWithArtists.artists.map { it.artistId }.toSet()
                if (currentArtistIds.intersect(seedArtistIds).isNotEmpty()) {
                    score += 50.0
                }

                // 3. Thời lượng gần nhau (+20 điểm nếu chênh lệch < 30s)
                val durationDiff = abs(songWithArtists.song.duration - seedSong.song.duration)
                if (durationDiff < 30000) { // 30 giây
                    score += 20.0
                }

                // 4. Tên bài hát có chứa từ giống nhau (+ N điểm)
                val seedWords = seedSong.song.name.split(" ").toSet()
                val commonWords = songWithArtists.song.name.split(" ").count { it in seedWords }
                score += commonWords * 2.0

                songWithArtists.song to score // Trả về đối tượng Song và điểm số
            }
            .sortedByDescending { it.second } // Sắp xếp điểm từ cao xuống thấp
            .map { it.first } // Chỉ lấy đối tượng Song
            .take(limit)
            .toList()
    }
}
