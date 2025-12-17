package com.ptit.btl_mobile.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

data class FileInfo(val filename: String, val size: Long)

fun getFileInfoFromUri(context: Context, uri: Uri): FileInfo? {
    context.contentResolver.query(
        uri,
        null,
        null,
        null,
        null
    ).use { cursor ->
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val indexSize = cursor.getColumnIndexOrThrow(OpenableColumns.SIZE)
                val indexName = cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
                return FileInfo(cursor.getString(indexName), cursor.getLong(indexSize))
            }
        }
    }
    return null
}