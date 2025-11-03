package com.ptit.btl_mobile.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.ptit.btl_mobile.R

@Composable
fun ThumbnailImage(
    imageUri: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    size: Dp? = null,           // THÊM MỚI
    isCircle: Boolean = false    // THÊM MỚI
) {
    // Xác định shape dựa trên isCircle
    val shape = if (isCircle) CircleShape else RoundedCornerShape(5.dp)

    // Áp dụng size nếu có
    val finalModifier = if (size != null) {
        Modifier
            .size(size)
            .clip(shape = shape)
            .then(modifier)
    } else {
        Modifier
            .clip(shape = shape)
            .then(modifier)
    }

    if (imageUri != null) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUri)
                .build(),
            placeholder = painterResource(R.drawable.ic_music_sample),
            contentDescription = "Song image",
            contentScale = contentScale,
            modifier = finalModifier
        )
    } else {
        Image(
            painter = painterResource(R.drawable.ic_music_sample),
            contentDescription = "Song image",
            contentScale = contentScale,
            modifier = finalModifier
        )
    }
}