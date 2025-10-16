package br.com.letrariaapp.ui.features.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.letrariaapp.R
import br.com.letrariaapp.data.*
import br.com.letrariaapp.ui.components.AppBackground
import br.com.letrariaapp.ui.components.ClubsSection
import br.com.letrariaapp.ui.theme.LetrariaAppTheme
import coil.compose.AsyncImage
import android.util.Log

// --- TELA "INTELIGENTE" (STATEFUL) ---
// É chamada pela navegação e gerencia a lógica e o estado.
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(),
    userId: String?,
    onBackClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    // NOVO LOG DE DIAGNÓSTICO
    Log.d("ProfileScreenDebug", "A função ProfileScreen foi chamada. UserID recebido: $userId")

    // Pede ao ViewModel para carregar os dados
    LaunchedEffect(key1 = userId) {
        // NOVO LOG DE DIAGNÓSTICO
        Log.d("ProfileScreenDebug", "O LaunchedEffect foi ativado, chamando viewModel.loadUserProfile...")
        viewModel.loadUserProfile(userId)
    }

    // Observa os dados vindos do ViewModel
    val user by viewModel.user.observeAsState()
    val isOwnProfile by viewModel.isOwnProfile.observeAsState(false)

    // Chama a tela "burra" passando os dados observados
    ProfileScreenContent(
        user = user,
        isOwnProfile = isOwnProfile,
        onBackClick = onBackClick,
        onEditProfileClick = onEditProfileClick,
        onLogoutClick = onLogoutClick,
        onAddFriendClick = { /* TODO: Lógica de adicionar amigo */ }
    )
}


// --- TELA "BURRA" (STATELESS) ---
// Apenas exibe a UI, não tem lógica de ViewModel. É esta que o Preview vai usar.
@Composable
fun ProfileScreenContent(
    user: User?,
    isOwnProfile: Boolean,
    onBackClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onAddFriendClick: () -> Unit
) {
    AppBackground(backgroundResId = R.drawable.background) {
        if (user == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                item {
                    ProfileTopAppBar(
                        isOwnProfile = isOwnProfile,
                        onBackClick = onBackClick,
                        onEditProfileClick = onEditProfileClick,
                        onLogoutClick = onLogoutClick,
                        onAddFriendClick = onAddFriendClick
                    )
                }
                item { ProfileHeader(name = user.name, imageUrl = user.profilePictureUrl) }
                item { FriendsSection(isOwnProfile = isOwnProfile, count = user.friendCount) }
                item { AboutMeSection(text = user.aboutMe) }
                item { BookChecklistSection(ratedBooks = user.checklist) }
                item { ClubsSection(title = "Clubes", clubs = user.clubs, onClubClick = {}) }
            }
        }
    }
}


// --- Componentes Menores (continuam os mesmos) ---

@Composable
fun ProfileTopAppBar(isOwnProfile: Boolean, onBackClick: () -> Unit, onEditProfileClick: () -> Unit, onLogoutClick: () -> Unit, onAddFriendClick: () -> Unit) {
    var menuExpanded by remember { mutableStateOf(false) }
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar") }
        if (isOwnProfile) {
            Box {
                IconButton(onClick = { menuExpanded = true }) { Icon(Icons.Default.MoreVert, contentDescription = "Configurações") }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(text = { Text("Editar Perfil") }, onClick = { menuExpanded = false; onEditProfileClick() })
                    DropdownMenuItem(text = { Text("Sair (Logout)") }, onClick = { menuExpanded = false; onLogoutClick() })
                }
            }
        }
    }
}

@Composable
fun ProfileHeader(name: String, imageUrl: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 16.dp)) {
        AsyncImage(model = imageUrl, contentDescription = "Foto de perfil de $name", modifier = Modifier.size(120.dp).clip(CircleShape), contentScale = ContentScale.Crop)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun FriendsSection(isOwnProfile: Boolean, count: Long) { // MUDANÇA: de Int para Long
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$count amigos",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        if (!isOwnProfile) {
            Button(onClick = { /* TODO: Lógica de adicionar amigo */ }) {
                Text("Adicionar amigo")
            }
        }
    }
}

@Composable
fun AboutMeSection(text: String) {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
        Text(text = if (text.isNotBlank()) text else "Nenhuma descrição adicionada.", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    }
}

@Composable
fun BookChecklistSection(ratedBooks: List<RatedBook>) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Checklist", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Button(onClick = { /* TODO: Adicionar livro */ }) { Text("Adicionar livro") }
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(ratedBooks) { ratedBook -> RatedBookCard(ratedBook = ratedBook) }
        }
    }
}

@Composable
fun RatedBookCard(ratedBook: RatedBook) {
    // MUDANÇA: Adicionamos esta verificação para garantir que o livro não é nulo
    if (ratedBook.book != null) {
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
                    tint = Color(0xFFFFC107)
                )
            }
        }
    }
}


// --- PREVIEWS ATUALIZADOS ---
// Agora podemos facilmente testar os dois cenários!

@Preview(name = "Meu Perfil", showBackground = true)
@Composable
fun OwnProfileScreenPreview() {
    LetrariaAppTheme {
        // O Preview chama a tela "burra" (Content) passando dados de exemplo
        ProfileScreenContent(
            user = sampleUser, // nosso usuário de exemplo
            isOwnProfile = true, // Dizendo que é nosso próprio perfil
            onBackClick = {},
            onEditProfileClick = {},
            onLogoutClick = {},
            onAddFriendClick = {}
        )
    }
}

@Preview(name = "Perfil de Visitante", showBackground = true)
@Composable
fun VisitorProfileScreenPreview() {
    LetrariaAppTheme {
        ProfileScreenContent(
            user = sampleUser,
            isOwnProfile = false, // Dizendo que NÃO é nosso perfil
            onBackClick = {},
            onEditProfileClick = {},
            onLogoutClick = {},
            onAddFriendClick = {}
        )
    }
}