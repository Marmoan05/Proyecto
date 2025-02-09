package com.example.apispotyp

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter

@Composable
fun TarjetaPequeniaPlaylist(playlistName: String, imageUrl: String) {
    Row(
        modifier = Modifier
            .width(180.dp)
            .height(70.dp)
            .background(Color(0xFF2A2A2A), shape = RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Cargar la imagen desde la URL con Coil
        Image(
            painter = rememberAsyncImagePainter(imageUrl),
            contentDescription = "Imagen de playlist",
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(8.dp))
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Nombre de la playlist
        Text(
            text = playlistName,
            style = TextStyle(color = Color.White, fontSize = 12.sp)
        )
    }
}
