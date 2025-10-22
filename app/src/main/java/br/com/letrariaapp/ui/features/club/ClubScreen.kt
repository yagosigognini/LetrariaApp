package br.com.letrariaapp.ui.features.club

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.letrariaapp.R
import br.com.letrariaapp.data.BookClub
import br.com.letrariaapp.data.Message
import br.com.letrariaapp.data.sampleUser
import br.com.letrariaapp.data.sampleClubsList
import br.com.letrariaapp.ui.components.AppBackground
import br.com.letrariaapp.ui.theme.LetrariaAppTheme
import coil.compose.AsyncImage
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

// --- TELA "INTELIGENTE" (STATEFUL) ---
@Composable
fun ClubScreen(
    clubId: String,
    viewModel: ClubViewModel,
    onBackClick: () -> Unit,
    onProfileClick: (String) -> Unit,
    onAdminClick: () -> Unit
) {
    LaunchedEffect(clubId) {
        viewModel.loadClubAndMessages(clubId)
    }

    val club by viewModel.club.observeAsState()
    val messages by viewModel.messages.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(true)
    val accessDenied by viewModel.accessDenied.observeAsState(false) // ✅ OBSERVA O NOVO ESTADO
    var inputText by remember { mutableStateOf("") }

    ClubScreenContent(
        club = club,
        messages = messages,
        inputText = inputText,
        onInputChange = { inputText = it },
        onSendMessage = {
            if (inputText.isNotBlank()) {
                viewModel.sendMessage(inputText, clubId)
                inputText = ""
            }
        },
        onSortearClick = { viewModel.drawUserForCycle() },
        isLoading = isLoading,
        accessDenied = accessDenied, // ✅ PASSA O NOVO ESTADO
        onBackClick = onBackClick,
        onProfileClick = onProfileClick,
        onAdminClick = onAdminClick
    )
}

// --- TELA "BURRA" (STATELESS) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubScreenContent(
    club: BookClub?,
    messages: List<Message>,
    inputText: String,
    onInputChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onSortearClick: () -> Unit,
    isLoading: Boolean,
    accessDenied: Boolean, // ✅ RECEBE O NOVO ESTADO
    onBackClick: () -> Unit,
    onProfileClick: (String) -> Unit,
    onAdminClick: () -> Unit
) {
    AppBackground(backgroundResId = R.drawable.background) {
        Scaffold(
            bottomBar = {
                // Só mostra a caixa de chat se o acesso NÃO for negado
                if (!accessDenied) {
                    MessageInput(
                        text = inputText,
                        onTextChange = onInputChange,
                        onSendClick = onSendMessage
                    )
                }
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding())
            ) {
                ClubHeader(
                    club = club,
                    onBackClick = onBackClick,
                    onAdminClick = onAdminClick
                )

                // ✅ LÓGICA DE EXIBIÇÃO ATUALIZADA
                if (isLoading) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (accessDenied) {
                    // Se o acesso for negado, mostra uma mensagem de erro
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            "Você não é membro deste clube.",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    // Se estiver tudo OK, mostra o painel e o chat
                    if (club != null) {
                        ActionPanel(
                            club = club,
                            onSortearClick = onSortearClick
                        )
                    }
                    MessageList(
                        messages = messages,
                        onProfileClick = onProfileClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// --- COMPONENTES DA TELA ---

@Composable
fun ActionPanel(club: BookClub, onSortearClick: () -> Unit) {
    val currentUserId = Firebase.auth.currentUser?.uid
    val isAdmin = currentUserId == club.adminId

    val sortedUserId = club.currentUserForCycleId
    val bookTitle = club.indicatedBookTitle

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (sortedUserId == null) {
                if (isAdmin) {
                    Text("Ninguém foi sorteado para indicar o próximo livro.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onSortearClick) {
                        Text("Sortear usuário")
                    }
                } else {
                    Text("Aguardando o admin sortear o próximo usuário.")
                }
            }
            else if (bookTitle == null) {
                Text("Usuário sorteado! Esperando indicação...")
            }
            else {
                Text("Livro indicado: $bookTitle")
            }
        }
    }
}

@Composable
fun ClubHeader(
    club: BookClub?,
    onBackClick: () -> Unit,
    onAdminClick: () -> Unit // ✅ PARÂMETRO ADICIONADO
) {
    val currentUserId = Firebase.auth.currentUser?.uid

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        AsyncImage(
            model = club?.imageUrl ?: "",
            contentDescription = "Capa do Clube",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.ic_launcher_background),
            error = painterResource(id = R.drawable.ic_launcher_background)
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                        startY = 100f
                    )
                )
        )
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
        }

        if (club != null && club.adminId == currentUserId) {
            IconButton(
                onClick = onAdminClick, // ✅ Ação conectada
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Configurações do Clube", tint = Color.White)
            }
        }

        Text(
            text = club?.name ?: "Carregando...",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        )
    }
}

@Composable
fun MessageList(
    messages: List<Message>,
    onProfileClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(messages) { message ->
            MessageItem(message = message, onProfileClick = onProfileClick)
        }
    }
}

@Composable
fun MessageItem(
    message: Message,
    onProfileClick: (String) -> Unit
) {
    val currentUserId = Firebase.auth.currentUser?.uid
    val isCurrentUser = message.senderId == currentUserId

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        Row(verticalAlignment = Alignment.Top) {
            if (!isCurrentUser) {
                AsyncImage(
                    model = message.senderPhotoUrl.ifEmpty { R.drawable.ic_launcher_background },
                    contentDescription = "Foto de ${message.senderName}",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable { onProfileClick(message.senderId) },
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column(horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start) {
                if (!isCurrentUser) {
                    Text(
                        text = message.senderName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    shape = RoundedCornerShape(
                        topStart = 8.dp, topEnd = 8.dp,
                        bottomStart = if (isCurrentUser) 8.dp else 0.dp,
                        bottomEnd = if (isCurrentUser) 0.dp else 8.dp
                    ),
                    color = if (isCurrentUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = message.text,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
                Text(
                    text = message.timestamp.toFormattedString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun MessageInput(
    text: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit
) {
    Surface(shadowElevation = 8.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Digite uma mensagem") },
                shape = RoundedCornerShape(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onSendClick) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar")
            }
        }
    }
}

private fun Date?.toFormattedString(): String {
    if (this == null) return ""
    return try {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.format(this)
    } catch (e: Exception) {
        ""
    }
}

// --- PREVIEW ---
@Preview(showBackground = true)
@Composable
fun ClubScreenContentPreview() {
    val sampleMessages = listOf(
        Message(senderName = "Yago", text = "Olá pessoal! Vamos começar a ler?", timestamp = Date()),
        Message(senderName = "Fulano", text = "Claro! Já estou na página 50.", timestamp = Date())
    )
    LetrariaAppTheme {
        ClubScreenContent(
            club = sampleClubsList.first(),
            messages = sampleMessages,
            inputText = "Minha nova mensagem",
            onInputChange = {},
            onSendMessage = {},
            onSortearClick = {},
            isLoading = false,
            accessDenied = false, // ✅ CORREÇÃO: Adicionando o parâmetro que faltava
            onBackClick = {},
            onProfileClick = {},
            onAdminClick = {}
        )
    }
}