package com.ptit.btl_mobile.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.ptit.btl_mobile.R

@Composable
fun ThumbnailImage(imageUri: String?, modifier: Modifier = Modifier, contentScale: ContentScale = ContentScale.Fit) {
    if (imageUri != null){
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUri)
                .build(),
            placeholder = painterResource(R.drawable.ic_music_sample),
            contentDescription = "Song image",
            contentScale = contentScale,
            modifier = Modifier.clip(shape = RoundedCornerShape(5.dp)).then(modifier)
        )
    }
    else{
        Image(
            painter = painterResource(R.drawable.ic_music_sample),
            contentDescription = "Song image",
            contentScale = contentScale,
            modifier = Modifier.clip(shape = RoundedCornerShape(5.dp)).then(modifier)
        )
    }
}