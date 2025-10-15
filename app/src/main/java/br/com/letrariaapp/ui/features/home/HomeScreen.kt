package br.com.letrariaapp.ui.features.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape // <-- IMPORT FALTANTE
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.letrariaapp.R // <-- IMPORT FALTANTE
import br.com.letrariaapp.data.BookClub
import br.com.letrariaapp.data.Quote
import br.com.letrariaapp.data.User
import br.com.letrariaapp.ui.components.AppBackground
import br.com.letrariaapp.ui.components.ClubItemCard
import br.com.letrariaapp.ui.theme.LetrariaAppTheme
import coil.compose.AsyncImage
import br.com.letrariaapp.ui.components.ClubsSection

// ... (Dados de exemplo continuam os mesmos) ...
val sampleUserForHome = User(id = "1", name = "Clarice", profilePictureUrl = "https://i.imgur.com/gC52M5f.jpeg", friendCount = 0, aboutMe = "", checklist = emptyList(), clubs = emptyList())
val sampleQuote = Quote(text = "\"Tu te tornas eternamente responsável por aquilo que cativas.\"", source = "O Pequeno Príncipe – Antoine de Saint-Exupéry")
val sampleClubs = listOf(BookClub("1", "Clássicos da Ficção", "https://i.imgur.com/sI22NM8.jpeg", 7, 15), BookClub("2", "Terror e Suspense", "https://i.imgur.com/EZ919sM.jpeg", 12, 15), BookClub("3", "Fantasias Épicas", "https://i.imgur.com/83oA79G.jpeg", 5, 15))
val homeButtonColor = Color(0xFFF2A28D)
val homeTextColor = Color(0xFF2A3A6A)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    user: User,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onCreateClubClick: () -> Unit,
    onJoinClubClick: () -> Unit,
    onClubClick: (BookClub) -> Unit
) {
    AppBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopBar(
                    profileImageUrl = user.profilePictureUrl,
                    onProfileClick = onProfileClick,
                    onSettingsClick = onSettingsClick
                )
            }
        ) { paddingValues ->
            // MUDANÇA PRINCIPAL: Voltamos a usar uma Column como base
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues), // Padding do Scaffold
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(40.dp))

                // Seção da Citação
                QuoteSection(quote = sampleQuote)

                Spacer(modifier = Modifier.height(150.dp))

                // Seção de Clubes (agora horizontal novamente)
                ClubsSection(
                    title = "Meus Clubes",
                    clubs = sampleClubs,
                    onClubClick = onClubClick,
                    modifier = Modifier.fillMaxWidth()
                )

                // O SPACER "MÁGICO" QUE EMPURRA OS BOTÕES PARA BAIXO
                Spacer(modifier = Modifier.height(100.dp))

                // Botões de Ação na Base
                // Adicionamos um padding horizontal para alinhar com o resto
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    ActionButtons(
                        onCreateClubClick = onCreateClubClick,
                        onJoinClubClick = onJoinClubClick
                    )
                }

                // Um último spacer para dar um respiro da borda inferior
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun TopBar(profileImageUrl: String, onProfileClick: () -> Unit, onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = profileImageUrl,
            contentDescription = "Perfil",
            modifier = Modifier.size(48.dp).clip(CircleShape).clickable(onClick = onProfileClick),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.ic_launcher_background)
        )
        IconButton(onClick = onSettingsClick) {
            Icon(imageVector = Icons.Default.Settings, contentDescription = "Configurações", tint = homeTextColor, modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
fun QuoteSection(quote: Quote) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(text = quote.text, color = homeTextColor, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = quote.source, color = homeTextColor, style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic)
    }
}

@Composable
fun ActionButtons(onCreateClubClick: () -> Unit, onJoinClubClick: () -> Unit) {
    Row(
        // Removendo o horizontalArrangement para dar controle total aos weights
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        // Botão 1 ocupa metade do espaço
        Button(
            onClick = onCreateClubClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = homeButtonColor),
            modifier = Modifier.weight(1f) // Ocupa 50% do espaço
        ) {
            Text("CRIAR CLUBE", color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }

        // Espaço fixo entre os botões
        Spacer(modifier = Modifier.width(16.dp))

        // Botão 2 ocupa a outra metade do espaço
        Button(
            onClick = onJoinClubClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = homeButtonColor),
            modifier = Modifier.weight(1f) // Ocupa os outros 50%
        ) {
            Text("BUSCAR CLUBES", color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    LetrariaAppTheme {
        HomeScreen(user = sampleUserForHome, {}, {}, {}, {}, {})
    }
}