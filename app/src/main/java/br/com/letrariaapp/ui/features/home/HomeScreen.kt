package br.com.letrariaapp.ui.features.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.letrariaapp.R
import br.com.letrariaapp.data.BookClub
import br.com.letrariaapp.data.Quote
import br.com.letrariaapp.data.User
import br.com.letrariaapp.data.sampleClubsList
import br.com.letrariaapp.data.sampleQuote
import br.com.letrariaapp.data.sampleUser
import br.com.letrariaapp.ui.components.AppBackground
import br.com.letrariaapp.ui.components.ClubsSection
import br.com.letrariaapp.ui.theme.LetrariaAppTheme
import coil.compose.AsyncImage

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.systemBars

val homeButtonColor = Color(0xFFE57373)
val homeTextColor = Color(0xFF2A3A6A)

// --- TELA "INTELIGENTE" (STATEFUL) ---
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onCreateClubClick: () -> Unit,
    onJoinClubClick: () -> Unit,
    onClubClick: (BookClub) -> Unit
) {
    val user by viewModel.user.observeAsState()
    val clubs by viewModel.clubs.observeAsState(emptyList())

    HomeScreenContent(
        user = user,
        quote = sampleQuote,
        clubs = clubs,
        onProfileClick = onProfileClick,
        onSettingsClick = onSettingsClick,
        onCreateClubClick = onCreateClubClick,
        onJoinClubClick = onJoinClubClick,
        onClubClick = onClubClick
    )
}

// --- TELA "BURRA" (STATELESS) ---
@Composable
fun HomeScreenContent(
    user: User?,
    quote: Quote,
    clubs: List<BookClub>,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onCreateClubClick: () -> Unit,
    onJoinClubClick: () -> Unit,
    onClubClick: (BookClub) -> Unit
) {
    AppBackground(backgroundResId = R.drawable.app_background) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = WindowInsets.systemBars.asPaddingValues()
        ) {
            item {
                HomeTopBar(
                    profileImageUrl = user?.profilePictureUrl,
                    onProfileClick = onProfileClick,
                    onSettingsClick = onSettingsClick
                )
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
            item { QuoteSection(quote = quote) }
            item { Spacer(modifier = Modifier.height(32.dp)) }

            // ✅ --- INÍCIO DA LÓGICA DE ESTADO VAZIO ---
            if (clubs.isEmpty()) {
                item {
                    EmptyClubsSection() // Mostra a mensagem amigável
                }
            } else {
                item {
                    ClubsSection( // Mostra a lista de clubes
                        title = "Meus Clubes",
                        clubs = clubs,
                        onClubClick = onClubClick
                    )
                }
            }
            // ✅ --- FIM DA LÓGICA DE ESTADO VAZIO ---

            item { Spacer(modifier = Modifier.height(24.dp)) }
            item { ActionButtons(onCreateClubClick, onJoinClubClick) }
        }
    }
}

// --- COMPONENTES ---

@Composable
fun HomeTopBar(
    profileImageUrl: String?,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = profileImageUrl?.ifEmpty { R.drawable.ic_launcher_background },
            contentDescription = "Foto de Perfil",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .clickable(onClick = onProfileClick),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.ic_launcher_background),
            error = painterResource(id = R.drawable.ic_launcher_background)
        )

        IconButton(onClick = onSettingsClick) {
            Icon(Icons.Default.Settings, contentDescription = "Configurações", tint = homeTextColor)
        }
    }
}

@Composable
fun QuoteSection(quote: Quote) {
    Column(
        modifier = Modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = quote.text,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = homeTextColor
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = quote.source,
            style = MaterialTheme.typography.bodySmall,
            color = homeTextColor.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun ActionButtons(onCreateClubClick: () -> Unit, onJoinClubClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = onCreateClubClick,
            modifier = Modifier
                .weight(1f)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = homeButtonColor)
        ) {
            Text("CRIAR CLUBE", color = Color.White)
        }
        Button(
            onClick = onJoinClubClick,
            modifier = Modifier
                .weight(1f)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = homeButtonColor)
        ) {
            Text("BUSCAR CLUBES", color = Color.White)
        }
    }
}

// ✅ --- NOVO COMPOSABLE ADICIONADO ---
@Composable
fun EmptyClubsSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 16.dp), // Padding para centralizar
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Você ainda não está em nenhum clube",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = homeTextColor
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Crie um novo clube ou use a busca para encontrar um clube e participar!",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = homeTextColor.copy(alpha = 0.8f)
        )
    }
}

// --- PREVIEW ---

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    LetrariaAppTheme {
        HomeScreenContent(
            user = sampleUser,
            quote = sampleQuote,
            clubs = sampleClubsList, // O preview ainda mostra a lista cheia
            onProfileClick = {},
            onSettingsClick = {},
            onCreateClubClick = {},
            onJoinClubClick = {},
            onClubClick = {}
        )
    }
}

// ✅ --- NOVO PREVIEW PARA O ESTADO VAZIO ---
@Preview(showBackground = true, name = "Home Screen Vazia")
@Composable
fun HomeScreenEmptyPreview() {
    LetrariaAppTheme {
        HomeScreenContent(
            user = sampleUser,
            quote = sampleQuote,
            clubs = emptyList(), // Passando uma lista vazia
            onProfileClick = {},
            onSettingsClick = {},
            onCreateClubClick = {},
            onJoinClubClick = {},
            onClubClick = {}
        )
    }
}