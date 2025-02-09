package com.example.apispotyp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.apispotyp.ui.theme.ApispotypTheme
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse

class MainActivity : AppCompatActivity() {

    private val REQUEST_CODE = 1337
    private val CLIENT_ID = "e42d03e83bc64c7485315823a0cac030"
    private val REDIRECT_URI = "com.example.apispotyp://callback"
    private val spotifyAuthManager = SpotifyAuthManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        Log.d("MainActivity", "Actividad iniciada")
        setContent {
            Aplicacion()
        }
    }

    override fun onResume() {
        super.onResume()

        val sharedPreferences = getSharedPreferences("SpotifyPrefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("ACCESS_TOKEN", null)
        val expirationTime = sharedPreferences.getLong("TOKEN_EXPIRATION", 0)
        Log.d("MainActivity", "Token ha expirado? ${isTokenExpired(expirationTime)}")

        if (token.isNullOrEmpty() || isTokenExpired(expirationTime)) {
            loginWithSpotify()
        }
    }

    /**
     * Verifica si el token ha expirado.
     * @param expirationTime Tiempo de expiración del token en milisegundos.
     * @return `true` si el token ha expirado, `false` si aún es válido.
     */
    private fun isTokenExpired(expirationTime: Long): Boolean {
        val currentTime = System.currentTimeMillis()
        return expirationTime == 0L || currentTime >= expirationTime
    }

    private fun loginWithSpotify() {
        val builder = AuthorizationRequest.Builder(
            CLIENT_ID,
            AuthorizationResponse.Type.TOKEN,
            REDIRECT_URI
        )
        builder.setScopes(arrayOf("user-read-private", "user-read-email", "playlist-read-private", "user-read-recently-played", "user-top-read"))
        builder.setShowDialog(true)
        val request = builder.build()
        AuthorizationClient.openLoginActivity(this, REQUEST_CODE, request)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == REQUEST_CODE) {
            val response = AuthorizationClient.getResponse(resultCode, intent)
            when (response.type) {
                AuthorizationResponse.Type.TOKEN -> {
                    val accessToken = response.accessToken
                    val expiresIn = response.expiresIn
                    Log.d("SpotifyAuth", "Token recibido: $accessToken, expira en: $expiresIn segundos")
                    val sharedPreferences = getSharedPreferences("SpotifyPrefs", MODE_PRIVATE)
                    val expirationTime = System.currentTimeMillis() + (expiresIn * 1000)
                    with(sharedPreferences.edit()) {
                        putString("ACCESS_TOKEN", accessToken)
                        putLong("TOKEN_EXPIRATION", expirationTime)
                        apply()
                    }
                }
                AuthorizationResponse.Type.ERROR -> {
                    Log.e("SpotifyAuth", "Error en autenticación: ${response.error}")
                }
                else -> {
                    Log.e("SpotifyAuth", "Autenticación cancelada o sin respuesta")
                }
            }
        }
    }
}