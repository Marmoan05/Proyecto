package com.example.apispotyp

import android.app.Activity
import android.util.Log
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.graphicsLayer
import coil.compose.rememberAsyncImagePainter

@Composable
fun PantallaInicio(onProfileClick: () -> Unit) {
    val context = LocalContext.current
    val spotifyAuthManager = remember { SpotifyAuthManager(context) }
    var mixDiarioPlaylists by remember { mutableStateOf<List<Triple<String, String, String>>>(emptyList()) }
    var playlistsRecientes by remember { mutableStateOf<List<Triple<String, String, String>>>(emptyList()) }
    var token by remember { mutableStateOf<String?>(null) }
    var isAuthenticated by remember { mutableStateOf(false) }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }
    var userDisplayName by remember { mutableStateOf<String?>(null) }
    var artistasMasEscuchados by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var cancionesArtista1 by remember { mutableStateOf<List<Triple<String, String, String>>>(emptyList()) }
    var cancionesArtista2 by remember { mutableStateOf<List<Triple<String, String, String>>>(emptyList()) }
    var cancionesArtista3 by remember { mutableStateOf<List<Triple<String, String, String>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        token = spotifyAuthManager.getAccessToken()
        if (token.isNullOrEmpty() && context is Activity) {
            spotifyAuthManager.authenticate(context)
            while (token.isNullOrEmpty()) {
                delay(500)
                token = spotifyAuthManager.getAccessToken()
            }
        }
        if (!token.isNullOrEmpty()) {
            isAuthenticated = true
            val userProfile = spotifyAuthManager.getUserProfile(token!!)
            profileImageUrl = userProfile?.images?.firstOrNull()?.url
            userDisplayName = userProfile?.display_name
        }
    }

    LaunchedEffect(token, isAuthenticated) {
        if (isAuthenticated && !token.isNullOrEmpty()) {
            delay(500)
            isLoading = true
            mixDiarioPlaylists = cargarPlaylists(token!!, spotifyAuthManager)
            delay(300)
            playlistsRecientes = cargarRecientes(token!!, spotifyAuthManager)
            delay(200)
            artistasMasEscuchados = spotifyAuthManager.getTopArtists(token!!)?.map { it.id to it.name } ?: emptyList()
            Log.d("SpotifyTest", artistasMasEscuchados.toString())
            delay(100)
            cancionesArtista1 = cargarAlbuneslistsDe(token!!, spotifyAuthManager, artistasMasEscuchados.firstOrNull()?.first ?: "")
            delay(100)
            cancionesArtista2 = cargarAlbuneslistsDe(token!!, spotifyAuthManager, artistasMasEscuchados.getOrNull(1)?.first ?: "")
            delay(100)
            cancionesArtista3 = cargarAlbuneslistsDe(token!!, spotifyAuthManager, artistasMasEscuchados.getOrNull(2)?.first ?: "")
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                profileImageUrl?.let { imageUrl ->
                    Image(
                        painter = rememberAsyncImagePainter(imageUrl),
                        contentDescription = "Imagen de perfil",
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .clickable { onProfileClick() }
                            .graphicsLayer {
                                scaleX = 1.2f
                                scaleY = 1.2f
                            }
                    )
                } ?: run {
                    Image(
                        painter = painterResource(id = R.drawable.pfp),
                        contentDescription = "Imagen de perfil",
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .clickable { onProfileClick() }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // If still loading, show loading spinner
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                } else {
                    if (!isAuthenticated) {
                        Text(text = "Esperando autenticación...", color = Color.White)
                    } else {
                        SeccionHorizontal(
                            title = "Hecho para ${userDisplayName ?: "Usuario"}",
                            playlists = mixDiarioPlaylists,
                            imageSize = 135.dp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        SeccionHorizontal(
                            title = "Recientes",
                            playlists = playlistsRecientes,
                            imageSize = 110.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    SeccionHorizontal(
                        title = "De ${artistasMasEscuchados.firstOrNull()?.second ?: "Artista"}",
                        playlists = cancionesArtista1,
                        imageSize = 135.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    SeccionHorizontal(
                        title = "De ${artistasMasEscuchados.getOrNull(1)?.second ?: "Artista"}",
                        playlists = cancionesArtista2,
                        imageSize = 135.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    SeccionHorizontal(
                        title = "De ${artistasMasEscuchados.getOrNull(2)?.second ?: "Artista"}",
                        playlists = cancionesArtista3,
                        imageSize = 135.dp
                    )
                }
            }
        }
    }
}

private suspend fun cargarAlbuneslistsDe(
    token: String,
    authManager: SpotifyAuthManager,
    id: String
): List<Triple<String, String, String>> {
    val result = mutableListOf<Triple<String, String, String>>()
    val query = id

    try {
        val albums = authManager.getAlbumsByArtist(token, query)

        if (!albums.isNullOrEmpty()) {
            albums.take(5).forEach { album ->
                val name = album.name.takeIf { it.isNotEmpty() }?.let {
                    if (it.length > 30) it.substring(0, 30) + "..." else it
                } ?: "Sin título"
                val imageUrl = album.images.firstOrNull()?.url ?: ""
                result.add(Triple(name, imageUrl, album.external_urls?.spotify ?: ""))
            }
        } else {
            Log.e("PantallaInicio", "No se encontraron álbumes con el query: $query")
        }
    } catch (e: Exception) {
        Log.e("PantallaInicio", "Error al cargar álbumes: ${e.message}")
    }

    return result
}

private suspend fun cargarRecientes(
    token: String,
    authManager: SpotifyAuthManager
): List<Triple<String, String, String>> {
    return try {
        val recientes = authManager.getRecentlyPlayed(token)
        recientes.map { trackItem ->
            // Limitar el nombre a 30 caracteres + "..." si es más largo
            val name = trackItem.name.takeIf { !it.isNullOrEmpty() }?.let {
                if (it.length > 30) it.substring(0, 27) + "..." else it
            } ?: "Sin título"

            val imageUrl = trackItem.album.images.firstOrNull()?.url ?: ""
            val spotifyUrl = trackItem.external_urls?.spotify ?: ""

            Triple(name, imageUrl, spotifyUrl)
        }
    } catch (e: Exception) {
        Log.e("PantallaInicio", "Error al cargar los recientes: ${e.message}")
        emptyList()
    }
}

private suspend fun cargarPlaylists(
    token: String,
    authManager: SpotifyAuthManager
): List<Triple<String, String, String>> {
    val result = mutableListOf<Triple<String, String, String>>()
    val nombresMixes = listOf("Mix diario 1", "Mix diario 2", "Mix diario 3", "Mix diario 4", "Mix diario 5")

    for (nombre in nombresMixes) {
        try {
            // Llama a searchPlaylistsByName y toma el primer resultado (si existe)
            val playlist = authManager.searchPlaylistByName(token, nombre)
            if (playlist != null) {
                // Usa safe calls y trim() para evitar problemas con espacios o null
                val playlistName = playlist.name.trim().ifEmpty { "Nombre desconocido" }
                val imageUrl = playlist.images.firstOrNull()?.url ?: ""
                val spotifyUrl = playlist.external_urls?.spotify ?: ""
                result.add(Triple(playlistName, imageUrl, spotifyUrl))
            } else {
                Log.e("PantallaInicio", "Playlist '$nombre' no encontrada")
            }
        } catch (e: Exception) {
            Log.e("PantallaInicio", "Error al cargar la playlist '$nombre': ${e.message}")
        }
    }
    return result
}


