package br.com.letrariaapp.ui.features.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.res.painterResource
import br.com.letrariaapp.R
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.letrariaapp.data.BookClub
import br.com.letrariaapp.data.Quote
import br.com.letrariaapp.ui.components.AppBackground // <-- ADICIONE ESTE IMPORT
import coil.compose.AsyncImage
import br.com.letrariaapp.ui.theme.LetrariaAppTheme

// --- Dados de Exemplo ---
val sampleQuote = Quote(
    text = "\"Tu te tornas eternamente responsável por aquilo que cativas.\"",
    source = "O Pequeno Príncipe – Antoine de Saint-Exupéry"
)

val sampleClubs = listOf(
    BookClub("1", "Clássicos da Ficção", "https://i.imgur.com/sI22NM8.jpeg", 7, 15),
    BookClub("2", "Terror e Suspense", "https://i.imgur.com/EZ919sM.jpeg", 12, 15),
    BookClub("3", "Fantasias Épicas", "https://i.imgur.com/83oA79G.jpeg", 5, 15)
)

// --- Cores do Design ---
val homeButtonColor = Color(0xFFF2A28D)
val homeTextColor = Color(0xFF2A3A6A)

// --- Composable Principal da Tela Home ---
@Composable
fun HomeScreen(
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onCreateClubClick: () -> Unit,
    onJoinClubClick: () -> Unit,
    onClubClick: (BookClub) -> Unit
) {
    // MUDANÇA 1: Usando o AppBackground para a imagem de fundo
    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Elemento 1: Barra do Topo (Perfil e Configurações)
            TopBar(onProfileClick = onProfileClick, onSettingsClick = onSettingsClick)

            Spacer(modifier = Modifier.height(40.dp))

            // Elemento 2: Seção da Citação
            QuoteSection(quote = sampleQuote,)

            // MUDANÇA 2: Adicionando um espaço onde antes ficava a curva
            Spacer(modifier = Modifier.height(40.dp))

            // Elemento 4: Lista de Clubes
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                items(sampleClubs) { club ->
                    ClubItemCard(club = club, onClick = { onClubClick(club) })
                }
            }

            // Elemento 5: Botões de Ação na Base
            ActionButtons(
                onCreateClubClick = onCreateClubClick,
                onJoinClubClick = onJoinClubClick
            )
        }
    }
}


// --- Componentes que formam a tela (sem alterações) ---

@Composable
fun TopBar(onProfileClick: () -> Unit, onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onProfileClick,
            modifier = Modifier
                .size(48.dp)
                .background(homeTextColor, CircleShape)
        ) {
            Icon(imageVector = Icons.Default.Person, contentDescription = "Perfil", tint = Color.White)
        }
        IconButton(onClick = onSettingsClick) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Configurações",
                tint = homeTextColor,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun QuoteSection(quote: Quote) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp)
    ) {
        Text(
            text = quote.text,
            color = homeTextColor,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = quote.source, color = homeTextColor, fontSize = 14.sp, fontStyle = FontStyle.Italic)
    }
}

@Composable
fun ClubItemCard(club: BookClub, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(contentAlignment = Alignment.BottomStart) {
            AsyncImage(
                model = club.imageUrl,
                contentDescription = "Imagem do clube ${club.name}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                // --- CORREÇÃO AQUI ---
                // Adicione estas duas linhas. Elas fornecem uma imagem local
                // caso a da internet não possa ser carregada (como no Preview).
                placeholder = painterResource(id = R.drawable.ic_launcher_background),
                error = painterResource(id = R.drawable.ic_launcher_background)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            startY = 200f
                        )
                    )
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = club.name, color = Color.White, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = "Membros",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${club.currentMembers}/${club.maxMembers}", color = Color.White, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun ActionButtons(onCreateClubClick: () -> Unit, onJoinClubClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp), // Reduzi o padding aqui
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
    ) {
        Button(
            onClick = onCreateClubClick,
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = homeButtonColor),
            modifier = Modifier.weight(1f)
        ) {
            Text("CRIAR NOVO CLUBE", color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
        Button(
            onClick = onJoinClubClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = homeButtonColor),
            modifier = Modifier.weight(1f)
        ) {
            Text("PARTICIPAR DE CLUBES", color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}


// --- Preview para ver o resultado no Android Studio ---
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    // Envelopando com o tema do app
    LetrariaAppTheme {
        HomeScreen({}, {}, {}, {}, {})
    }
}