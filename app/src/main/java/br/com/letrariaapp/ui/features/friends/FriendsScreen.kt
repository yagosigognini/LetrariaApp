package br.com.letrariaapp.ui.features.friends

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
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
import br.com.letrariaapp.data.Friend
import br.com.letrariaapp.data.FriendRequest
import br.com.letrariaapp.data.sampleUser
import br.com.letrariaapp.ui.components.AppBackground
import br.com.letrariaapp.ui.theme.LetrariaAppTheme
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import java.util.Date

// --- TELA "INTELIGENTE" (STATEFUL) ---
@Composable
fun FriendsScreen(
    viewModel: FriendsViewModel = viewModel(),
    onBackClick: () -> Unit,
    onSearchUserClick: () -> Unit, // Navega para a tela de busca de usuários
    onProfileClick: (String) -> Unit // Navega para o perfil de um amigo
) {
    val state by viewModel.state.collectAsState()
    // ✅ Observa o novo estado para o diálogo de confirmação
    val friendToRemove by viewModel.friendToRemove.collectAsState()

    // ✅ Mostra o diálogo de confirmação se friendToRemove não for nulo
    friendToRemove?.let { friend ->
        RemoveFriendDialog(
            friendName = friend.name,
            onDismiss = { viewModel.cancelRemoveFriend() }, // Ação de Cancelar
            onConfirm = { viewModel.removeFriend(friend) } // Ação de Confirmar
        )
    }

    FriendsScreenContent(
        state = state,
        onBackClick = onBackClick,
        onSearchUserClick = onSearchUserClick,
        onProfileClick = onProfileClick,
        onAcceptRequest = { request -> viewModel.acceptFriendRequest(request) },
        onDeclineRequest = { request -> viewModel.declineFriendRequest(request) },
        // ✅ ATUALIZADO: Chama 'requestRemoveFriend' (que mostra o diálogo)
        // em vez de 'removeFriend' (que deleta direto)
        onRemoveFriendRequest = { friend -> viewModel.requestRemoveFriend(friend) },
        onCancelRequest = { request -> viewModel.cancelSentRequest(request) }
    )
}

// --- TELA "BURRA" (STATELESS) ---
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FriendsScreenContent(
    state: FriendsScreenState,
    onBackClick: () -> Unit,
    onSearchUserClick: () -> Unit,
    onProfileClick: (String) -> Unit,
    onAcceptRequest: (FriendRequest) -> Unit,
    onDeclineRequest: (FriendRequest) -> Unit,
    onRemoveFriendRequest: (Friend) -> Unit, // ✅ ATUALIZADO: Nome do parâmetro
    onCancelRequest: (FriendRequest) -> Unit
) {
    val tabTitles = listOf("Amigos", "Pedidos Recebidos", "Pedidos Enviados")
    val pagerState = rememberPagerState { tabTitles.size }
    val coroutineScope = rememberCoroutineScope()

    AppBackground(backgroundResId = R.drawable.background) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Amizades") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    windowInsets = WindowInsets.statusBars
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = onSearchUserClick) {
                    Icon(Icons.Default.Search, contentDescription = "Buscar Novos Amigos")
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                SecondaryTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = Color.Transparent
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            text = { Text(title) },
                            selectedContentColor = MaterialTheme.colorScheme.primary,
                            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { page ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = if (state.isLoading && page == 0) Alignment.Center else Alignment.TopStart
                    ) {
                        if (state.isLoading && page == 0) {
                            CircularProgressIndicator()
                        } else {
                            when (page) {
                                0 -> FriendsList(
                                    friends = state.friends,
                                    onProfileClick = onProfileClick,
                                    onRemoveRequestClick = onRemoveFriendRequest // ✅ ATUALIZADO
                                )
                                1 -> ReceivedRequestsList(
                                    requests = state.receivedRequests,
                                    onProfileClick = onProfileClick,
                                    onAcceptClick = onAcceptRequest,
                                    onDeclineClick = onDeclineRequest
                                )
                                2 -> SentRequestsList(
                                    requests = state.sentRequests,
                                    onCancelClick = onCancelRequest
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Componentes das Abas ---

@Composable
fun FriendsList(
    friends: List<Friend>,
    onProfileClick: (String) -> Unit,
    onRemoveRequestClick: (Friend) -> Unit // ✅ ATUALIZADO
) {
    if (friends.isEmpty()) {
        Text("Você ainda não tem amigos.", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 16.dp))
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(friends, key = { it.uid }) { friend ->
                FriendItemRow(
                    friend = friend,
                    onProfileClick = { onProfileClick(friend.uid) },
                    onRemoveRequestClick = { onRemoveRequestClick(friend) } // ✅ ATUALIZADO
                )
            }
        }
    }
}

@Composable
fun ReceivedRequestsList(
    requests: List<FriendRequest>,
    onProfileClick: (String) -> Unit,
    onAcceptClick: (FriendRequest) -> Unit,
    onDeclineClick: (FriendRequest) -> Unit
) {
    // ... (Sem mudanças aqui) ...
    if (requests.isEmpty()) {
        Text("Nenhum pedido de amizade recebido.", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 16.dp))
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(requests, key = { it.senderId }) { request ->
                FriendRequestItemRow(
                    request = request,
                    onProfileClick = { onProfileClick(request.senderId) },
                    onAcceptClick = { onAcceptClick(request) },
                    onDeclineClick = { onDeclineClick(request) }
                )
            }
        }
    }
}

@Composable
fun SentRequestsList(
    requests: List<FriendRequest>,
    onCancelClick: (FriendRequest) -> Unit
) {
    // ... (Sem mudanças aqui) ...
    if (requests.isEmpty()) {
        Text("Nenhum pedido de amizade enviado.", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 16.dp))
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(requests, key = { it.receiverId }) { request ->
                SentRequestItemRow(
                    request = request,
                    onCancelClick = { onCancelClick(request) }
                )
            }
        }
    }
}

// --- Componentes dos Itens da Lista ---

@Composable
fun FriendItemRow(
    friend: Friend,
    onProfileClick: () -> Unit,
    onRemoveRequestClick: () -> Unit, // ✅ ATUALIZADO
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onProfileClick)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = friend.profilePictureUrl.ifEmpty { R.drawable.ic_launcher_background },
                contentDescription = "Foto de ${friend.name}",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.ic_launcher_background),
                error = painterResource(id = R.drawable.ic_launcher_background)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(friend.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.weight(1f))
            // ✅ Botão de Remover agora chama o request
            IconButton(onClick = onRemoveRequestClick) {
                Icon(Icons.Default.Close, contentDescription = "Remover Amigo", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun FriendRequestItemRow(
    // ... (Sem mudanças aqui) ...
    request: FriendRequest,
    onProfileClick: () -> Unit,
    onAcceptClick: () -> Unit,
    onDeclineClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onProfileClick)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = request.senderPhotoUrl.ifEmpty { R.drawable.ic_launcher_background },
                contentDescription = "Foto de ${request.senderName}",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.ic_launcher_background),
                error = painterResource(id = R.drawable.ic_launcher_background)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(request.senderName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onDeclineClick) {
                Icon(Icons.Default.Close, contentDescription = "Recusar", tint = MaterialTheme.colorScheme.error)
            }
            IconButton(onClick = onAcceptClick) {
                Icon(Icons.Default.Check, contentDescription = "Aceitar", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun SentRequestItemRow(
    // ... (Sem mudanças aqui) ...
    request: FriendRequest,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = request.receiverPhotoUrl.ifEmpty { R.drawable.ic_launcher_background },
                contentDescription = "Foto de ${request.receiverName}",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.ic_launcher_background),
                error = painterResource(id = R.drawable.ic_launcher_background)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(request.receiverName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text("Pendente", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onCancelClick) {
                Icon(Icons.Default.Close, contentDescription = "Cancelar Pedido", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

// ⬇️ --- NOVO DIÁLOGO DE CONFIRMAÇÃO --- ⬇️
@Composable
fun RemoveFriendDialog(
    friendName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Remover Amigo") },
        text = { Text("Tem certeza que deseja remover \"${friendName}\" da sua lista de amigos?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Remover", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
// ⬆️ --- FIM DO NOVO DIÁLOGO --- ⬆️

// --- Preview ---
@Preview(showBackground = true)
@Composable
fun FriendsScreenContentPreview() {
    // ... (Dados de exemplo - sem mudanças) ...
    val sampleFriend = Friend(uid = "123", name = "Amigo Exemplo", profilePictureUrl = "", addedAt = Date())
    val sampleReceivedRequest = FriendRequest(senderId = "456", senderName = "Usuário Pedinte", receiverId = "123", receiverName = "Eu")
    val sampleSentRequest = FriendRequest(senderId = "123", senderName = "Eu", receiverId = "789", receiverName = "Pessoa Pendente")
    val sampleState = FriendsScreenState(isLoading = false, friends = listOf(sampleFriend), receivedRequests = listOf(sampleReceivedRequest), sentRequests = listOf(sampleSentRequest))

    LetrariaAppTheme {
        FriendsScreenContent(
            state = sampleState,
            onBackClick = {},
            onSearchUserClick = {},
            onProfileClick = {},
            onAcceptRequest = {},
            onDeclineRequest = {},
            onRemoveFriendRequest = {}, // ✅ ATUALIZADO
            onCancelRequest = {}
        )
    }
}