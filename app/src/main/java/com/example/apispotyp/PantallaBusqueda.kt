package com.example.apispotyp
import TarjetaHorizontal
import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PantallaBusqueda(onProfileClick: () -> Unit) {
    val context = LocalContext.current
    val spotifyAuthManager = remember { SpotifyAuthManager(context) }
    var token by remember { mutableStateOf<String?>(null) }
    var searchText by remember { mutableStateOf("") }
    var searchActive by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<Triple<String, String, String>>?>(null) }
    val coroutineScope = rememberCoroutineScope()
    var profileImageUrl by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        token = spotifyAuthManager.getAccessToken()
        if (token.isNullOrEmpty() && context is Activity) {
            spotifyAuthManager.authenticate(context)
            while (token.isNullOrEmpty()) {
                delay(500)
                token = spotifyAuthManager.getAccessToken()
            }
        }
        val userProfile = spotifyAuthManager.getUserProfile(token!!)
        profileImageUrl = userProfile?.images?.firstOrNull()?.url
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Buscar",
                    style = TextStyle(color = Color.White, fontSize = 24.sp)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(Color.White, shape = RoundedCornerShape(25.dp))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (searchActive) Icons.Default.Close else Icons.Default.Search,
                contentDescription = if (searchActive) "Limpiar búsqueda" else "Buscar",
                tint = Color.Gray,
                modifier = Modifier
                    .size(20.dp)
                    .clickable {
                        if (searchActive) {
                            searchText = ""
                            searchResults = emptyList()
                            searchActive = false
                        } else {
                            if (searchText.isNotEmpty() && token != null) {
                                searchActive = true
                                coroutineScope.launch {
                                    val response = spotifyAuthManager.searchAll(token!!, searchText)
                                    val results = response?.playlists?.items?.mapNotNull { playlist ->
                                        if (playlist != null) {
                                            Triple(
                                                playlist.name,
                                                playlist.images.firstOrNull()?.url ?: "",
                                                playlist.external_urls.spotify
                                            )
                                        } else {
                                            null
                                        }
                                    } ?: emptyList()
                                    searchResults = results
                                }
                            }
                        }
                    }
            )
            Spacer(modifier = Modifier.width(8.dp))
            BasicTextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(color = Color.Black),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        if (searchText.isEmpty()) {
                            Text(
                                text = "¿Qué te apetece buscar?",
                                color = Color.Gray,
                                style = TextStyle(fontSize = 14.sp)
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (searchActive && searchResults != null) {
            LazyColumn {
                items(searchResults!!) { result ->
                    TarjetaHorizontal(
                        playlistName = result.first,
                        imageUrl = result.second,
                        imageSize = 165.dp,
                        urlPlaylist = result.third
                    )
                }
            }
        } else {
            ContenidoMusica()
        }
    }
}
