package br.com.letrariaapp.ui.features.club

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.ui.platform.LocalContext // ✅ IMPORT ADICIONADO
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction // ✅ IMPORT ADICIONADO
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.letrariaapp.R
import br.com.letrariaapp.data.BookClub
import br.com.letrariaapp.data.IndicatedBook
import br.com.letrariaapp.data.Message
import br.com.letrariaapp.data.User
import br.com.letrariaapp.data.sampleClubsList
import br.com.letrariaapp.data.sampleUser
import br.com.letrariaapp.ui.components.AppBackground
import br.com.letrariaapp.ui.theme.LetrariaAppTheme
import coil.compose.AsyncImage
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

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
    val accessDenied by viewModel.accessDenied.observeAsState(false)
    val sortedUser by viewModel.sortedUser.observeAsState(null)
    var inputText by remember { mutableStateOf("") }

    val context = LocalContext.current // ✅ CONTEXTO PARA O TOAST DO NOVO BOTÃO

    ClubScreenContent(
        club = club,
        messages = messages,
        sortedUser = sortedUser,
        inputText = inputText,
        onInputChange = { inputText = it },
        onSendMessage = {
            if (inputText.isNotBlank()) {
                viewModel.sendMessage(inputText, clubId)
                inputText = ""
            }
        },
        onSortearClick = { viewModel.drawUserForCycle() },
        onIndicarLivroClick = { title, author, publisher, cycleDays ->
            viewModel.indicateBook(clubId, title, author, publisher, cycleDays)
        },
        isLoading = isLoading,
        accessDenied = accessDenied,
        onBackClick = onBackClick,
        onProfileClick = onProfileClick,
        onAdminClick = onAdminClick,
        // ✅ AÇÃO PARA O NOVO BOTÃO DE MENU
        onMenuClick = {
            // TODO: Substituir isso por um BottomSheet com as opções
            Toast.makeText(context, "Menu de opções (Gamificação, etc)", Toast.LENGTH_SHORT).show()
        }
    )
}

// --- TELA "BURRA" (STATELESS) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubScreenContent(
    club: BookClub?,
    messages: List<Message>,
    sortedUser: User?,
    inputText: String,
    onInputChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onSortearClick: () -> Unit,
    onIndicarLivroClick: (String, String, String, Int) -> Unit,
    isLoading: Boolean,
    accessDenied: Boolean,
    onBackClick: () -> Unit,
    onProfileClick: (String) -> Unit,
    onAdminClick: () -> Unit,
    onMenuClick: () -> Unit // ✅ PARÂMETRO ADICIONADO
) {
    AppBackground(backgroundResId = R.drawable.background) {
        Scaffold(
            bottomBar = {
                if (!accessDenied) {
                    MessageInput(
                        text = inputText,
                        onTextChange = onInputChange,
                        onSendClick = onSendMessage,
                        onMenuClick = onMenuClick // ✅ PASSANDO A AÇÃO PARA O INPUT
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

                if (club != null && !accessDenied) {
                    ActionPanel(
                        club = club,
                        sortedUser = sortedUser,
                        onSortearClick = onSortearClick,
                        onIndicarLivroClick = onIndicarLivroClick
                    )
                }

                if (isLoading) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (accessDenied) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            "Você não é membro deste clube.",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
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

// ... (ActionPanel, ClubHeader, MessageList, MessageItem - NENHUMA MUDANÇA AQUI) ...

@Composable
fun ActionPanel(
    club: BookClub,
    sortedUser: User?,
    onSortearClick: () -> Unit,
    onIndicarLivroClick: (String, String, String, Int) -> Unit
) {
    val currentUserId = Firebase.auth.currentUser?.uid
    val isAdmin = currentUserId == club.adminId
    val isSortedUser = currentUserId == club.currentUserForCycleId

    val sortedUserId = club.currentUserForCycleId
    val indicatedBook = club.indicatedBook

    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var publisher by remember { mutableStateOf("") }
    var cycleDays by remember { mutableStateOf(club.readingCycleDays.toString()) }

    Surface(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Estado 1: Ninguém foi sorteado
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

            // Estado 2: Sorteado, mas sem livro
            else if (indicatedBook == null) {
                if (isSortedUser) {
                    Text("Você foi sorteado! Indique o próximo livro:", textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título do Livro") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = author, onValueChange = { author = it }, label = { Text("Autor") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = publisher, onValueChange = { publisher = it }, label = { Text("Editora") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = cycleDays,
                        onValueChange = { cycleDays = it.filter { char -> char.isDigit() } },
                        label = { Text("Tempo de leitura (dias)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        onIndicarLivroClick(title, author, publisher, cycleDays.toIntOrNull() ?: 15)
                    }) {
                        Text("Indicar Livro")
                    }
                } else {
                    val sortedUserName = sortedUser?.name ?: "..."
                    Text("Esperando @${sortedUserName} indicar um livro.", textAlign = TextAlign.Center)
                }
            }

            // Estado 3: Livro indicado
            else {
                val now = System.currentTimeMillis()
                val endDate = club.cycleEndDate ?: now
                val daysRemaining = if (endDate < now) 0 else TimeUnit.MILLISECONDS.toDays(endDate - now)

                val textToShow = when {
                    daysRemaining > 1 -> "Faltam $daysRemaining dias para o ciclo acabar"
                    daysRemaining == 1L -> "Falta 1 dia para o ciclo acabar"
                    else -> "Ciclo encerrado!"
                }

                Text(textToShow, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Livro: ${indicatedBook.title} por ${indicatedBook.author}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))

                if (isAdmin && daysRemaining < 1) {
                    Button(onClick = onSortearClick) {
                        Text("Sortear novo usuário")
                    }
                }
            }
        }
    }
}

@Composable
fun ClubHeader(
    club: BookClub?,
    onBackClick: () -> Unit,
    onAdminClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
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
                        startY = 50f
                    )
                )
                .windowInsetsPadding(WindowInsets.statusBars)
        )
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
        }

        IconButton(
            onClick = onAdminClick,
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
        ) {
            Icon(Icons.Default.Settings, contentDescription = "Configurações do Clube", tint = Color.White)
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

// ✅ --- FUNÇÃO MESSAGEINPUT TOTALMENTE ATUALIZADA ---
@Composable
fun MessageInput(
    text: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onMenuClick: () -> Unit // ✅ Parâmetro para o novo botão de menu
) {
    Surface(shadowElevation = 8.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ✅ Botão de Menu (Três barrinhas)
            // Usei o FilledTonalIconButton para dar o fundo circular que você mostrou
            FilledTonalIconButton(onClick = onMenuClick) {
                Icon(
                    Icons.Default.Menu,
                    contentDescription = "Opções"
                )
            }
            Spacer(modifier = Modifier.width(8.dp))

            // ✅ Campo de Texto
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f), // Ocupa o espaço restante
                placeholder = { Text("Digite uma mensagem") },
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                // Ações do teclado (para enviar com "Enter")
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = { onSendClick() }
                ),
                // ✅ Botão de Enviar (Avião) DENTRO do campo de texto
                trailingIcon = {
                    IconButton(onClick = onSendClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Enviar"
                        )
                    }
                }
            )
            // ❌ O Spacer e o IconButton(Send) que estavam aqui foram removidos
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
    val clubState3 = sampleClubsList.first().copy(
        currentUserForCycleId = "123",
        indicatedBook = IndicatedBook(title = "O Pequeno Príncipe", author = "Antoine de Saint-Exupéry"),
        cycleEndDate = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(6)
    )

    LetrariaAppTheme {
        ClubScreenContent(
            club = clubState3,
            messages = sampleMessages,
            sortedUser = sampleUser,
            inputText = "Minha nova mensagem",
            onInputChange = {},
            onSendMessage = {},
            onSortearClick = {},
            onIndicarLivroClick = { _, _, _, _ -> },
            isLoading = false,
            accessDenied = false,
            onBackClick = {},
            onProfileClick = {},
            onAdminClick = {},
            onMenuClick = {} // ✅ ADICIONADO AO PREVIEW
        )
    }
}