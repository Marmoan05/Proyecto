import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter



@Composable
fun TarjetaHorizontal(
    playlistName: String,
    imageUrl: String,
    imageSize: Dp,
    urlPlaylist: String
) {
    val context = LocalContext.current
    // Contenedor para la tarjeta horizontal
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlPlaylist))
                intent.setPackage("com.spotify.music") // Intenta abrir en la app de Spotify

                try {
                    context.startActivity(intent) // Si Spotify está instalado, lo abre
                } catch (e: ActivityNotFoundException) {
                    // Si no está instalado, abre en el navegador
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(urlPlaylist)))
                }
            },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1C)) // Color oscuro para la tarjeta
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen de la playlist
            Image(
                painter = rememberAsyncImagePainter(imageUrl),
                contentDescription = playlistName,
                modifier = Modifier
                    .size(imageSize)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Nombre de la playlist
            Text(
                text = playlistName,
                style = TextStyle(color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
