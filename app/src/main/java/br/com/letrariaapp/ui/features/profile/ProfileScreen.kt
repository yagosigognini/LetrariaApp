package br.com.letrariaapp.ui.features.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import br.com.letrariaapp.data.User
import br.com.letrariaapp.data.sampleClubsList
import br.com.letrariaapp.data.sampleUser
import br.com.letrariaapp.ui.components.AppBackground
import br.com.letrariaapp.ui.components.ClubsSection
import br.com.letrariaapp.ui.theme.LetrariaAppTheme
import coil.compose.AsyncImage

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(),
    userId: String?,
    onBackClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onClubClick: (BookClub) -> Unit // ✅ PARÂMETRO ADICIONADO
) {
    LaunchedEffect(key1 = userId) {
        viewModel.loadUserProfile(userId)
    }

    val user by viewModel.user.observeAsState()
    val isOwnProfile by viewModel.isOwnProfile.observeAsState(false)
    val clubs by viewModel.clubs.observeAsState(emptyList())

    if (user == null) {
        AppBackground(backgroundResId = R.drawable.background) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    } else {
        ProfileScreenContent(
            user = user!!,
            isOwnProfile = isOwnProfile,
            clubs = clubs,
            onBackClick = onBackClick,
            onEditProfileClick = onEditProfileClick,
            onLogoutClick = onLogoutClick,
            onSettingsClick = onSettingsClick,
            onClubClick = onClubClick // ✅ PARÂMETRO PASSADO
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreenContent(
    user: User,
    isOwnProfile: Boolean,
    clubs: List<BookClub>,
    onBackClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onClubClick: (BookClub) -> Unit // ✅ PARÂMETRO ADICIONADO
) {
    AppBackground(backgroundResId = R.drawable.background) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                        }
                    },
                    actions = {
                        if (isOwnProfile) {
                            IconButton(onClick = onSettingsClick) {
                                Icon(Icons.Default.Settings, contentDescription = "Configurações")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    ProfileHeader(user = user, isOwnProfile = isOwnProfile, onEditProfileClick = onEditProfileClick)
                }
                item {
                    ChecklistSection()
                }
                item {
                    ClubsSection(
                        title = "Clubes",
                        clubs = clubs,
                        onClubClick = onClubClick // ✅ CONEXÃO FEITA AQUI
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileHeader(
    user: User,
    isOwnProfile: Boolean,
    onEditProfileClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        AsyncImage(
            model = user.profilePictureUrl.ifEmpty { R.drawable.ic_launcher_background },
            contentDescription = "Foto de Perfil",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.ic_launcher_background),
            error = painterResource(id = R.drawable.ic_launcher_background)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = user.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "${user.friendCount} amigos",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (isOwnProfile) {
            Button(onClick = onEditProfileClick) {
                Text("Editar Perfil")
            }
        } else {
            Button(onClick = { /* TODO: Adicionar amigo */ }) {
                Text("Adicionar amigo")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = user.aboutMe,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ChecklistSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Checklist",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Button(onClick = { /* TODO: Adicionar livro */ }) {
                Text("Adicionar livro")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Aqui virá a lista de livros...
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    LetrariaAppTheme {
        ProfileScreenContent(
            user = sampleUser,
            isOwnProfile = true,
            clubs = sampleClubsList,
            onBackClick = {},
            onEditProfileClick = {},
            onLogoutClick = {},
            onSettingsClick = {},
            onClubClick = {} // ✅ ADICIONADO AO PREVIEW
        )
    }
}