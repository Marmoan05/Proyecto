package com.example.apispotyp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Base64
import android.util.Log
import com.github.scribejava.core.builder.ServiceBuilder
import com.github.scribejava.core.builder.api.DefaultApi20
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

class SpotifyAuthManager(private val context: Context) {
    companion object {
        private const val CLIENT_ID = "e42d03e83bc64c7485315823a0cac030"
        private const val CLIENT_SECRET = "3dc87a54c457442c8051401e107e9320"
        private const val REDIRECT_URI = "com.example.apispotyp://callback"
        private const val SCOPE = "user-read-private user-read-email playlist-read-private user-read-recently-played user-top-read"
        private const val AUTH_REQUEST_CODE = 1337
    }


    private val oauthService = ServiceBuilder(CLIENT_ID)
        .apiSecret(CLIENT_SECRET)
        .defaultScope(SCOPE)
        .callback(REDIRECT_URI)
        .build(SpotifyApi.instance())

    fun getAuthorizationUrl(): String {
        return oauthService.authorizationUrl
    }

    fun authenticate(activity: Activity) {
        val request = AuthorizationRequest.Builder(
            CLIENT_ID,
            AuthorizationResponse.Type.TOKEN,
            REDIRECT_URI
        )
            .setScopes(arrayOf("user-read-private", "user-read-email", "playlist-read-private", "user-read-recently-played", "user-top-read"))
            .build()
        AuthorizationClient.openLoginActivity(activity, AUTH_REQUEST_CODE, request)
    }

    fun handleAuthResponse(requestCode: Int, resultCode: Int, data: Intent?, callback: (String?) -> Unit) {
        if (requestCode == AUTH_REQUEST_CODE) {
            val response = AuthorizationClient.getResponse(resultCode, data)
            when (response.type) {
                AuthorizationResponse.Type.TOKEN -> {
                    val token = response.accessToken
                    Log.d("SpotifyAuthManager", "Token recibido: $token")
                    saveAccessToken(token)
                    callback(token)
                }
                AuthorizationResponse.Type.ERROR -> {
                    Log.e("SpotifyAuthManager", "Error en autenticación: ${response.error}")
                    callback(null)
                }
                else -> {
                    Log.e("SpotifyAuthManager", "Autenticación cancelada o sin respuesta")
                    callback(null)
                }
            }
        }
    }

    fun saveAccessToken(token: String) {
        if (token.isNotEmpty()) {
            val sharedPreferences = context.getSharedPreferences("SpotifyPrefs", Context.MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putString("ACCESS_TOKEN", token)
                apply()
            }
        } else {
            Log.e("SpotifyAuthManager", "Token vacío, no se puede guardar.")
        }
    }

    fun getAccessToken(): String? {
        val sharedPreferences = context.getSharedPreferences("SpotifyPrefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("ACCESS_TOKEN", null)
        Log.d("SpotifyAuthManager", "Token en SharedPreferences: $token")
        return token
    }

    fun saveRefreshToken(refreshToken: String) {
        val sharedPreferences = context.getSharedPreferences("SpotifyPrefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("REFRESH_TOKEN", refreshToken)
            apply()
        }
    }

    fun getRefreshToken(): String? {
        val sharedPreferences = context.getSharedPreferences("SpotifyPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("REFRESH_TOKEN", null)
    }

    suspend fun refreshAccessToken(): String? {
        val refreshToken = getRefreshToken() ?: return null
        val url = "https://accounts.spotify.com/api/token"
        val body = FormBody.Builder()
            .add("grant_type", "refresh_token")
            .add("refresh_token", refreshToken)
            .add("client_id", CLIENT_ID)
            .add("client_secret", CLIENT_SECRET)
            .build()
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()
        return try {
            val response = OkHttpClient().newCall(request).execute()
            if (response.isSuccessful) {
                val jsonResponse = JSONObject(response.body?.string() ?: "")
                val newAccessToken = jsonResponse.getString("access_token")
                saveAccessToken(newAccessToken)
                newAccessToken
            } else {
                Log.e("SpotifyAuthManager", "Error al refrescar el token: ${response.code}")
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ---------------------------
    // Métodos de búsqueda usando Retrofit (SpotifyService)
    // ---------------------------
    interface SpotifyService {
        @GET("v1/search")
        suspend fun searchPlaylists(
            @Header("Authorization") accessToken: String,
            @Query("q") query: String,
            @Query("type") type: String = "playlist"
        ): SearchResponse

        @GET("v1/me")
        suspend fun getUserProfile(
            @Header("Authorization") accessToken: String
        ): UserProfile

        @GET("v1/search")
        suspend fun searchTracks(
            @Header("Authorization") accessToken: String,
            @Query("q") query: String,
            @Query("type") type: String = "track"
        ): TrackSearchResponse

        @GET("v1/browse/categories/{category_id}/playlists")
        suspend fun getPlaylistsByCategory(
            @Header("Authorization") accessToken: String,
            @Path("category_id") categoryId: String
        ): SearchResponse

        @GET("v1/users/{user_id}/playlists")
        suspend fun getUserPlaylistsByUserId(
            @Header("Authorization") accessToken: String,
            @Path("user_id") userId: String
        ): SearchResponse

        @GET("v1/me/player/recently-played")
        suspend fun getRecentlyPlayed(
            @Header("Authorization") accessToken: String,
            @Query("limit") limit: Int = 9
        ): RecentlyPlayedResponse

        @GET("v1/me/top/artists")
        suspend fun getTopArtists(
            @Header("Authorization") accessToken: String,
            @Query("limit") limit: Int = 3
        ): TopArtistsResponse

        // Nuevo endpoint: Obtener los álbumes de un artista
        @GET("v1/artists/{artist_id}/albums")
        suspend fun getAlbumsByArtist(
            @Header("Authorization") accessToken: String,
            @Path("artist_id") artistId: String,
            @Query("include_groups") includeGroups: String = "album,single",
            @Query("limit") limit: Int = 20
        ): AlbumsResponse
        @GET("v1/search")
        suspend fun searchAll(
            @Header("Authorization") accessToken: String,
            @Query("q") query: String,
            @Query("type") type: String = "album,artist,playlist,track,show,episode,audiobook",
            @Query("market") market: String = "ES",
            @Query("limit") limit: Int = 50
        ): SearchAllResponse

        @GET("v1/browse/categories")
        suspend fun getCategories(
            @Header("Authorization") accessToken: String,
            @Query("locale") locale: String = "es_ES",
            @Query("limit") limit: Int = 50
        ): CategoriesResponse

    }

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.spotify.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val service = retrofit.create(SpotifyService::class.java)

    /**
     * Busca todas las playlists cuyo nombre coincida con el query.
     */
    suspend fun searchPlaylistsByName(accessToken: String, query: String): List<Playlist>? {
        return try {
            val response = service.searchPlaylists(
                accessToken = "Bearer $accessToken",
                query = query
            )
            response.playlists.items
        } catch (e: Exception) {
            Log.e("SpotifyAuthManager", "Error en searchPlaylistsByName: ${e.message}")
            null
        }
    }

    /**
     * Busca una playlist exacta por nombre (devuelve la primera que coincida ignorando mayúsculas).
     */
    suspend fun searchPlaylistByName(accessToken: String, query: String): Playlist? {
        val playlists = searchPlaylistsByName(accessToken, query)
        return playlists?.filterNotNull()?.firstOrNull { it.name.equals(query, ignoreCase = true) }
    }

    /**
     * Busca canciones (tracks) por nombre.
     */

    data class Track(val name: String, val imageUrl: String, val artist: String)
    suspend fun searchTracksByName(accessToken: String, query: String): List<Track>? {
        return try {
            val response = service.searchTracks(
                accessToken = "Bearer $accessToken",
                query = query
            )
            response.tracks.items
        } catch (e: Exception) {
            Log.e("SpotifyAuthManager", "Error en searchTracksByName: ${e.message}")
            null
        }
    }

    /**
     * Busca playlists por categoría usando el ID de la categoría.
     */
    suspend fun searchPlaylistsByCategory(accessToken: String, categoryId: String): List<Playlist>? {
        return try {
            val response = service.getPlaylistsByCategory(
                accessToken = "Bearer $accessToken",
                categoryId = categoryId
            )
            response.playlists.items
        } catch (e: Exception) {
            Log.e("SpotifyAuthManager", "Error en searchPlaylistsByCategory: ${e.message}")
            null
        }
    }

    /**
     * Obtiene todas las playlists públicas de un usuario y filtra aquellas cuyo nombre contenga el query.
     */
    suspend fun searchUserPlaylistsByName(accessToken: String, userId: String, query: String): List<Playlist>? {
        return try {
            val response = service.getUserPlaylistsByUserId(
                accessToken = "Bearer $accessToken",
                userId = userId
            )
            response.playlists.items.filter { it.name.contains(query, ignoreCase = true) }
        } catch (e: Exception) {
            Log.e("SpotifyAuthManager", "Error en searchUserPlaylistsByName: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    suspend fun getUserProfile(accessToken: String): UserProfile? {
        return try {
            val response = service.getUserProfile("Bearer $accessToken")
            Log.d("SpotifyAuthManager", "Response: $response")
            response
        } catch (e: Exception) {
            Log.e("SpotifyAuthManager", "Error al obtener el perfil del usuario: ${e.message}")
            null
        }
    }


    suspend fun getRecentlyPlayed(accessToken: String): List<TrackItem> {
        return try {
            Log.d("SpotifyAuthManager", "Usando token: Bearer $accessToken")  // Agrega este log
            val response = service.getRecentlyPlayed("Bearer $accessToken")
            response.items.map { it.track }
        } catch (e: Exception) {
            Log.e("SpotifyAuthManager", "Error en getRecentlyPlayed: ${e.message}")
            emptyList()
        }
    }

    suspend fun getTopArtists(accessToken: String): List<ArtistItem>? {
        return try {
            val response = service.getTopArtists("Bearer $accessToken", limit = 3)
            response.items
        } catch (e: Exception) {
            Log.e("SpotifyAuthManager", "Error en getTopArtists: ${e.message}")
            null
        }
    }


    suspend fun getAlbumsByArtist(accessToken: String, artistId: String): List<Album>? {
        return withContext(Dispatchers.IO) {
            try {
                val response = service.getAlbumsByArtist("Bearer $accessToken", artistId)
                response.items
            } catch (e: Exception) {
                Log.e("SpotifyAuthManager", "Error en getAlbumsByArtist: ${e.message}")
                null
            }
        }
    }

    suspend fun searchAll(accessToken: String, query: String): SearchAllResponse? {
        return withContext(Dispatchers.IO) {
            try {
                val response = service.searchAll("Bearer $accessToken", query)
                response
            } catch (e: Exception) {
                Log.e("SpotifyAuthManager", "Error en searchAll: ${e.message}")
                null
            }
        }
    }
    //getcategories
    suspend fun getCategories(accessToken: String): CategoriesResponse? {
        return withContext(Dispatchers.IO){
            try {
                val response = service.getCategories("Bearer $accessToken")
                Log.d("SpotifyAuthManager", "Response: $response")
                response
            } catch (e: Exception) {
                Log.e("SpotifyAuthManager", "Error en getCategories: ${e.message}")
                null
            }
        }
    }


    // ---------------------------
    // Modelos de datos
    // ---------------------------
    // Respuesta general de búsqueda para todos los tipos
    data class SearchAllResponse(
        val albums: AlbumList?,
        val artists: ArtistList?,
        val playlists: PlaylistList?,
        val tracks: TrackList?,
        val shows: ShowList?,
        val episodes: EpisodeList?,
        val audiobooks: AudiobookList?
    )


    data class AlbumList(
        val items: List<Album>
    )

    data class ArtistList(
        val items: List<Artist>
    )

    data class PlaylistList(
        val items: List<Playlist>
    )

    data class TrackList(
        val items: List<Track>
    )

    data class ShowList(
        val items: List<Show>
    )

    data class EpisodeList(
        val items: List<Episode>
    )

    data class AudiobookList(
        val items: List<Audiobook>
    )


    data class Album(
        val name: String,
        val images: List<Image>,
        val external_urls: ExternalUrls
    )

    data class Artist(
        val name: String,
        val images: List<Image>,
        val external_urls: ExternalUrls
    )

    data class Playlist(
        val name: String,
        val images: List<Image>,
        val external_urls: ExternalUrls
    )


    data class Show(
        val name: String,
        val images: List<Image>,
        val external_urls: ExternalUrls
    )

    data class Episode(
        val name: String,
        val images: List<Image>,
        val external_urls: ExternalUrls
    )

    data class Audiobook(
        val name: String,
        val images: List<Image>,
        val external_urls: ExternalUrls
    )

    data class Image(
        val url: String
    )

    data class ExternalUrls(
        val spotify: String
    )

    data class TokenResponse(
        val access_token: String,
        val token_type: String,
        val expires_in: Int
    )
    data class TopArtistsResponse(
        val items: List<ArtistItem>
    )

    data class ArtistItem(
        val id: String,
        val name: String,
        val images: List<Image>?,
        val followers: Followers?,
        val external_urls: ExternalUrls
    )


    data class SearchResponse(
        val playlists: PlaylistList
    )

    data class ArtistSearchResponse(
        val artists: ArtistList
    )

    data class AlbumsResponse(
        val items: List<Album>
    )
    data class UserProfile(
        val display_name: String?,
        val images: List<Image>?,
        val followers: Followers?,
        val external_urls: ExternalUrls
    )
    data class Followers(
        val href: String?,
        val total: Int
    )

    data class TrackSearchResponse(
        val tracks: TrackList
    )


    data class TrackItem(
        val name: String,
        val album: Album,
        val artists: List<Artist>,
        val external_urls: ExternalUrls
    )

    data class CategoriesResponse(
        val categories: CategoryList
    )

    data class CategoryList(
        val items: List<Category>
    )


    data class RecentlyPlayedResponse(
        val items: List<RecentlyPlayedItem>
    )

    data class RecentlyPlayedItem(
        val track: TrackItem
    )

    data class Category(
        val name: String,
        val icons: List<Image>
    )



    // ---------------------------
    // Clase interna para la API de Spotify
    // ---------------------------
    class SpotifyApi private constructor() : DefaultApi20() {
        override fun getAccessTokenEndpoint(): String {
            return "https://accounts.spotify.com/api/token"
        }
        override fun getAuthorizationBaseUrl(): String {
            return "https://accounts.spotify.com/authorize"
        }
        companion object {
            fun instance(): SpotifyApi {
                return SpotifyApi()
            }
        }
    }
}