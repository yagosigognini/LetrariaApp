// Em ui/features/profile/ProfileScreen.kt
package br.com.letrariaapp.ui.features.profile

import androidx.compose.foundation.Image
import br.com.letrariaapp.ui.components.ClubItemCard
import br.com.letrariaapp.ui.components.ClubsSection
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.letrariaapp.R
import br.com.letrariaapp.data.*
import br.com.letrariaapp.ui.theme.LetrariaAppTheme
import coil.compose.AsyncImage

// --- Dados de Exemplo ---
val sampleUser = User(
    id = "1",
    name = "Clarice Lispector",
    profilePictureUrl = "https://i.imgur.com/gC52M5f.jpeg",
    friendCount = 67,
    aboutMe = "Escritora e jornalista ucraniana de origem judaica russa, naturalizada brasileira e radicada no Brasil. Autora de romances, contos e ensaios, é considerada uma das escritoras brasileiras mais importantes do século XX.",
    checklist = listOf(
        RatedBook(Book("c1", "A Hora da Estrela", "Clarice Lispector", "https://i.imgur.com/u382xNp.jpeg"), 4),
        RatedBook(Book("c2", "A Paixão Segundo G.H.", "Clarice Lispector", "https://i.imgur.com/sI22NM8.jpeg"), 5),
        RatedBook(Book("c3", "Perto do Coração Selvagem", "Clarice Lispector", "https://i.imgur.com/EZ919sM.jpeg"), 4)
    ),
    clubs = listOf(
        BookClub("1", "Clássicos da Ficção", "https://i.imgur.com/sI22NM8.jpeg", 7, 15),
        BookClub("2", "Terror e Suspense", "https://i.imgur.com/EZ919sM.jpeg", 12, 15)
    )
)

// --- Composable Principal ---
@Composable
fun ProfileScreen(
    user: User,
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // LazyColumn permite que a tela role se o conteúdo for grande
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Barra do Topo
            item {
                ProfileTopAppBar(onBackClick = onBackClick, onSettingsClick = onSettingsClick)
            }

            // Cabeçalho com Foto e Nome
            item {
                ProfileHeader(
                    name = user.name,
                    imageUrl = user.profilePictureUrl
                )
            }

            // Seção de Amigos
            item {
                FriendsSection(count = user.friendCount)
            }

            // Seção "Sobre Mim"
            item {
                AboutMeSection(text = user.aboutMe)
            }

            // Seção Checklist de Livros
            item {
                BookChecklistSection(ratedBooks = user.checklist)
            }

            // Seção de Clubes
            item {
                ClubsSection(
                    title = "Clubes",
                    clubs = user.clubs,
                    onClubClick = { /* TODO */ }
                )
            }
        }
    }
}

// --- Componentes Menores que formam a tela ---

@Composable
fun ProfileTopAppBar(onBackClick: () -> Unit, onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = MaterialTheme.colorScheme.onBackground)
        }
        IconButton(onClick = onSettingsClick) {
            Icon(Icons.Default.Settings, contentDescription = "Configurações", tint = MaterialTheme.colorScheme.onBackground)
        }
    }
}

@Composable
fun ProfileHeader(name: String, imageUrl: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Foto de perfil de $name",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun FriendsSection(count: Int) {
    Text(
        text = "$count amigos",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 16.dp)
    )
}

@Composable
fun AboutMeSection(text: String) {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    }
}

@Composable
fun BookChecklistSection(ratedBooks: List<RatedBook>) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Checklist", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Button(onClick = { /* TODO: Adicionar livro */ }) {
                Text("Adicionar livro")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(ratedBooks) { ratedBook ->
                RatedBookCard(ratedBook = ratedBook)
            }
        }
    }
}

@Composable
fun RatedBookCard(ratedBook: RatedBook) {
    Column(
        modifier = Modifier.width(120.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = ratedBook.book.coverUrl,
            contentDescription = ratedBook.book.title,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "${ratedBook.rating}/5", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                Icons.Default.Star,
                contentDescription = "Nota",
                modifier = Modifier.size(16.dp),
                tint = Color.Yellow // Ou uma cor do seu tema
            )
        }
    }
}


// --- Preview ---
@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    LetrariaAppTheme {
        ProfileScreen(user = sampleUser, onBackClick = {}, onSettingsClick = {})
    }
}