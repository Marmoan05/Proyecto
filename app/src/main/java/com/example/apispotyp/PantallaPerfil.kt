package com.example.apispotyp

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.delay

@Composable
fun PantallaPerfil() {
    val context = LocalContext.current
    val spotifyAuthManager = remember { SpotifyAuthManager(context) }
    var userProfile by remember { mutableStateOf<SpotifyAuthManager.UserProfile?>(null) }
    var recentlyAutorsPlayed by remember { mutableStateOf<List<SpotifyAuthManager.ArtistItem>?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        isLoading = true
        val token = spotifyAuthManager.getAccessToken()
        if (!token.isNullOrEmpty()) {
            userProfile = spotifyAuthManager.getUserProfile(token)
            delay(500)
            recentlyAutorsPlayed = spotifyAuthManager.getTopArtists(token)
        }
        isLoading = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp)
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val imageUrl = userProfile?.images?.firstOrNull()?.url
                    val urlUser = userProfile?.external_urls?.spotify
                    Image(
                        painter = rememberAsyncImagePainter(imageUrl ?: R.drawable.pfp),
                        contentDescription = "Profile Image",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlUser))
                                intent.setPackage("com.spotify.music")

                                try {
                                    context.startActivity(intent)
                                } catch (e: ActivityNotFoundException) {
                                    // Si no está instalado, abre en el navegador
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(urlUser)))
                                }
                            },
                        contentScale = ContentScale.Crop,
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = userProfile?.display_name ?: "Nombre",
                            style = TextStyle(color = Color.White, fontSize = 24.sp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Seguidores: ${userProfile?.followers?.total ?: "N/A"}",
                            style = TextStyle(color = Color.Gray, fontSize = 14.sp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Artistas más escuchados recientemente",
                    style = TextStyle(color = Color.White, fontSize = 18.sp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                recentlyAutorsPlayed?.take(3)?.forEach { artist ->
                    Spacer(modifier = Modifier.height(8.dp))
                    TarjetaArtista(
                        artistName = artist.name,
                        followers = "Seguidores: ${artist.followers?.total ?: "N/A"}",
                        imageUrl = artist.images?.firstOrNull()?.url ?: "",
                        urlArtist = artist.external_urls?.spotify ?: ""
                    )
                }
            }
        }
    }
}
