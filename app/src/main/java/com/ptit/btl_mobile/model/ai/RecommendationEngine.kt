package com.ptit.btl_mobile.model.ai

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.android.gms.tasks.Task
import com.google.android.gms.tflite.java.TfLite
import com.ptit.btl_mobile.R
import com.ptit.btl_mobile.model.database.Song
import com.ptit.btl_mobile.model.database.SongWithArtists
import org.tensorflow.lite.InterpreterApi
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.getValue
import kotlin.math.abs

class RecommendationEngine(private val context: Context) {

    private lateinit var tfliteInterpreter: InterpreterApi
    private val MODEL_FILENAME = "recommendation_model.tflite"

    init {
        val initializeTask: Task<Void> by lazy { TfLite.initialize(context) }
        initializeTask.addOnSuccessListener {
            val interpreterOption =
                InterpreterApi.Options().setRuntime(InterpreterApi.Options.TfLiteRuntime.FROM_SYSTEM_ONLY)
            tfliteInterpreter = InterpreterApi.create(
                loadModelFile(),
                interpreterOption
            )}
            .addOnFailureListener { e ->
                Log.e("AI_MODEL", "Cannot initialize interpreter", e)
            }
    }

    private fun loadModelFile(): MappedByteBuffer {
        return try {
            val fileDescriptor = context.assets.openFd(MODEL_FILENAME)
            val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val startOffset = fileDescriptor.startOffset
            val declaredLength = fileDescriptor.declaredLength
            fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        } catch (e: Exception) {
            e.printStackTrace()
            error("Model file not found!!!")
        }
    }

    fun getRecommendations(seedSong: SongWithArtists, allSongs: List<SongWithArtists>, limit: Int): List<Song> {
        val recommendations = allSongs.map { songWithArtists ->
            // Chuẩn bị input (ví dụ: duration) - bạn có thể tùy chỉnh để khớp với model
            val input = floatArrayOf(
                seedSong.song.duration.toFloat(),
                songWithArtists.song.duration.toFloat()
            )
            val output = Array(1) { FloatArray(1) }

            try {
                tfliteInterpreter.run(input, output)
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
}
