package com.example.apispotyp

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import android.util.Log

@Composable
fun ContenidoMusica() {
    val context = LocalContext.current
    val spotifyAuthManager = remember { SpotifyAuthManager(context) }

    var categories by remember { mutableStateOf<List<SpotifyAuthManager.Category>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var token by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        token = spotifyAuthManager.getAccessToken()
        if (token.isNullOrEmpty() && context is Activity) {
            spotifyAuthManager.authenticate(context)
            while (token.isNullOrEmpty()) {
                delay(500)
                token = spotifyAuthManager.getAccessToken()
            }
        }
        token?.let {
            categories = spotifyAuthManager.getCategories(token!!)?.categories?.items
            Log.d("SpotifyTest", categories.toString())
        }
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 0.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Explorar todo",
            style = TextStyle(color = Color.White, fontSize = 18.sp),
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
        } else {
            categories?.chunked(2)?.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowItems.forEach { item ->
                        TarjetaCategoria(
                            name = item.name,
                            urlImagen = item.icons.firstOrNull()?.url ?: "",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Si hay un solo elemento en la fila, agregamos un `Spacer` para balancear
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
