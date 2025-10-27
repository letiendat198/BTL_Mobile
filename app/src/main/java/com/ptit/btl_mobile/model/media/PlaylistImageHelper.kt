package com.ptit.btl_mobile.model.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File

object PlaylistImageHelper {

    /**
     * Copy ảnh từ URI nguồn vào internal storage
     * @param context Context của app
     * @param sourceUri URI của ảnh được chọn từ gallery
     * @param playlistId ID của playlist (dùng để đặt tên file)
     * @return URI của ảnh đã lưu trong internal storage, hoặc null nếu thất bại
     */
    fun copyImageToInternal(context: Context, sourceUri: Uri, playlistId: Long): Uri? {
        return try {
            // Bước 1: Đọc bitmap từ URI nguồn
            val inputStream = context.contentResolver.openInputStream(sourceUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap != null) {
                // Bước 2: Resize ảnh nếu quá lớn (tiết kiệm dung lượng)
                val resizedBitmap = resizeBitmap(bitmap, 800, 800)

                // Bước 3: Lưu vào internal storage
                val imagePath = "playlist_images/${playlistId}.png"
                val savedUri = writeBitmapToInternal(context, resizedBitmap, imagePath)

                // Giải phóng bộ nhớ
                if (resizedBitmap != bitmap) {
                    bitmap.recycle()
                }
                resizedBitmap.recycle()

                Log.d("PlaylistImageHelper", "Image saved successfully: $savedUri")
                savedUri
            } else {
                Log.e("PlaylistImageHelper", "Failed to decode bitmap from URI: $sourceUri")
                null
            }
        } catch (e: Exception) {
            Log.e("PlaylistImageHelper", "Error copying image: ${e.message}", e)
            null
        }
    }

    /**
     * Lưu Bitmap vào internal storage
     * Tương tự như MediaLoader.writeBitmapToInternal
     */
    private fun writeBitmapToInternal(context: Context, bitmap: Bitmap, path: String): Uri {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val bytes = stream.toByteArray()

        val file = File(context.filesDir, path)
        file.parentFile?.mkdirs() // Tạo thư mục nếu chưa tồn tại
        file.createNewFile()
        file.writeBytes(bytes)

        return Uri.fromFile(file)
    }

    /**
     * Resize bitmap để không quá lớn
     * @param bitmap Bitmap gốc
     * @param maxWidth Chiều rộng tối đa
     * @param maxHeight Chiều cao tối đa
     * @return Bitmap đã resize (hoặc bitmap gốc nếu đã đủ nhỏ)
     */
    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        // Nếu ảnh đã nhỏ hơn kích thước tối đa, giữ nguyên
        if (width <= maxWidth && height <= maxHeight) {
            return bitmap
        }

        // Tính tỷ lệ scale
        val scale = minOf(maxWidth.toFloat() / width, maxHeight.toFloat() / height)
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    /**
     * Xóa ảnh của playlist khi xóa playlist
     * @param context Context của app
     * @param playlistId ID của playlist cần xóa ảnh
     */
    fun deletePlaylistImage(context: Context, playlistId: Long) {
        try {
            val file = File(context.filesDir, "playlist_images/${playlistId}.png")
            if (file.exists()) {
                val deleted = file.delete()
                if (deleted) {
                    Log.d("PlaylistImageHelper", "Deleted image for playlist $playlistId")
                } else {
                    Log.w("PlaylistImageHelper", "Failed to delete image for playlist $playlistId")
                }
            }
        } catch (e: Exception) {
            Log.e("PlaylistImageHelper", "Error deleting image: ${e.message}", e)
        }
    }

    /**
     * Kiểm tra xem playlist có ảnh custom không
     * @param context Context của app
     * @param playlistId ID của playlist
     * @return true nếu có ảnh, false nếu không
     */
    fun hasCustomImage(context: Context, playlistId: Long): Boolean {
        val file = File(context.filesDir, "playlist_images/${playlistId}.png")
        return file.exists()
    }

    /**
     * Lấy URI của ảnh playlist từ internal storage
     * @param context Context của app
     * @param playlistId ID của playlist
     * @return URI của ảnh, hoặc null nếu không tồn tại
     */
    fun getPlaylistImageUri(context: Context, playlistId: Long): Uri? {
        val file = File(context.filesDir, "playlist_images/${playlistId}.png")
        return if (file.exists()) {
            Uri.fromFile(file)
        } else {
            null
        }
    }
}