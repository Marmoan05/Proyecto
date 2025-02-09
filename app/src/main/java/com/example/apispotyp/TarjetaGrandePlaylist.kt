package com.example.apispotyp

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

@Composable
fun TarjetaGrandePlaylist(playlistName: String, imageUrl: String, imageSize: Dp, urlPlaylist: String) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .width(imageSize)
            .padding(end = 8.dp)
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlPlaylist))
                intent.setPackage("com.spotify.music")

                try {
                    context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(urlPlaylist)))
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = rememberAsyncImagePainter(imageUrl),
            contentDescription = "Imagen de playlist",
            modifier = Modifier
                .size(imageSize)
                .clip(RoundedCornerShape(10.dp))
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = playlistName,
            color = Color.White
        )
    }
}
