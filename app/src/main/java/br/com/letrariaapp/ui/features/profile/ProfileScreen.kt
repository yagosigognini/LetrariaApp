package br.com.letrariaapp.ui.features.profile



// --- Imports Essenciais ---

import android.util.Log // Import do Log

import android.widget.Toast

import androidx.compose.foundation.ExperimentalFoundationApi // ⭐️ Import para Long Press

import androidx.compose.foundation.background

import androidx.compose.foundation.clickable

import androidx.compose.foundation.combinedClickable // ⭐️ Import para Long Press

import androidx.compose.foundation.layout.*

import androidx.compose.foundation.lazy.LazyColumn

import androidx.compose.foundation.lazy.LazyRow

import androidx.compose.foundation.lazy.items

import androidx.compose.foundation.shape.CircleShape

import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.automirrored.filled.ArrowBack

import androidx.compose.material.icons.filled.Add

import androidx.compose.material.icons.filled.Settings

import androidx.compose.material.icons.filled.Star

import androidx.compose.material3.*

import androidx.compose.runtime.*

import androidx.compose.runtime.livedata.observeAsState

import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier

import androidx.compose.ui.draw.clip

import androidx.compose.ui.graphics.Color

import androidx.compose.ui.layout.ContentScale

import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.res.painterResource

import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.text.style.TextAlign

import androidx.compose.ui.text.style.TextOverflow

import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.ui.unit.dp

import androidx.compose.ui.unit.sp

import androidx.lifecycle.viewmodel.compose.viewModel

import br.com.letrariaapp.R

import br.com.letrariaapp.data.BookClub

import br.com.letrariaapp.data.ProfileRatedBook // Modelo correto para livros avaliados

import br.com.letrariaapp.data.User

import br.com.letrariaapp.data.sampleClubsList

import br.com.letrariaapp.data.sampleRatedBooks // Para Preview

import br.com.letrariaapp.data.sampleUser

import br.com.letrariaapp.ui.components.AppBackground

import br.com.letrariaapp.ui.components.ClubsSection

import br.com.letrariaapp.ui.features.home.homeButtonColor // Cor do botão

import br.com.letrariaapp.ui.theme.LetrariaAppTheme

import coil.compose.AsyncImage

import java.util.Locale // Para formatar nota



// --- TELA "INTELIGENTE" (STATEFUL) ---

@Composable

fun ProfileScreen(

    viewModel: ProfileViewModel = viewModel(),

    userId: String?, // ID do perfil sendo visualizado (pode ser null se for o próprio)

    onBackClick: () -> Unit,

    onEditProfileClick: () -> Unit,

    onLogoutClick: () -> Unit, // Mantido caso Settings precise

    onSettingsClick: () -> Unit,

    onClubClick: (BookClub) -> Unit,

    onAddBookClick: () -> Unit // Navega para BookSearch

) {

    LaunchedEffect(key1 = userId) {

        viewModel.loadUserProfile(userId) // Carrega o perfil correto

    }



    // Observa os estados do ViewModel

    val user by viewModel.user.observeAsState()

    val isOwnProfile by viewModel.isOwnProfile.observeAsState(false) // Define se é o perfil do usuário logado

    val clubs by viewModel.clubs.observeAsState(emptyList())

    val bookToRate by viewModel.bookToRate // Livro pendente de avaliação

    val ratedBooks by viewModel.ratedBooks.observeAsState(emptyList()) // Lista de livros avaliados

    val bookToDelete by viewModel.bookToDelete // Livro pendente de exclusão



    // Mostra diálogo de avaliação se houver livro pendente

    bookToRate?.let { book ->

        RatingDialog(

            bookTitle = book.volumeInfo?.title,

            onDismiss = { viewModel.onRatingDialogDismiss() },

            onSubmit = { rating -> viewModel.onRatingSubmitted(rating) }

        )

    }



    // ⭐️ Mostra diálogo de confirmação de exclusão se houver livro pendente

    bookToDelete?.let { book ->

        DeleteConfirmationDialog(

            bookTitle = book.title,

            onDismiss = { viewModel.cancelDeleteBook() },

            onConfirm = { viewModel.confirmDeleteBook() }

        )

    }



    // Tela de carregamento enquanto o usuário não foi carregado

    if (user == null) {

        AppBackground(backgroundResId = R.drawable.background) {

            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

                CircularProgressIndicator()

            }

        }

    } else {

        // Passa todos os estados e callbacks necessários para a tela "burra"

        ProfileScreenContent(

            user = user!!,

            isOwnProfile = isOwnProfile,

            clubs = clubs,

            ratedBooks = ratedBooks,

            onBackClick = onBackClick,

            onEditProfileClick = onEditProfileClick,

            onLogoutClick = onLogoutClick, // Passado adiante

            onSettingsClick = onSettingsClick,

            onClubClick = onClubClick,

            onAddBookClick = onAddBookClick,

            onDeleteBookClick = { book -> // ⭐️ Passa a ação de pedir exclusão

                viewModel.requestDeleteBook(book)

            }

        )

    }

}



// --- TELA "BURRA" (STATELESS) ---

@OptIn(ExperimentalMaterial3Api::class)

@Composable

fun ProfileScreenContent(

    user: User,

    isOwnProfile: Boolean,

    clubs: List<BookClub>,

    ratedBooks: List<ProfileRatedBook>,

    onBackClick: () -> Unit,

    onEditProfileClick: () -> Unit,

    onLogoutClick: () -> Unit, // Recebe o callback

    onSettingsClick: () -> Unit,

    onClubClick: (BookClub) -> Unit,

    onAddBookClick: () -> Unit,

    onDeleteBookClick: (ProfileRatedBook) -> Unit // ⭐️ Recebe o callback de exclusão

) {

    AppBackground(backgroundResId = R.drawable.background) {

        Scaffold(

            containerColor = Color.Transparent, // Fundo transparente para ver o AppBackground

            topBar = {

                TopAppBar(

                    title = { }, // Sem título na barra

                    navigationIcon = { // Botão de voltar

                        IconButton(onClick = onBackClick) {

                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")

                        }

                    },

                    actions = { // Botões à direita

                        // Mostra botão de Configurações APENAS no perfil próprio

                        if (isOwnProfile) {

                            IconButton(onClick = onSettingsClick) {

                                Icon(Icons.Default.Settings, contentDescription = "Configurações")

                            }

                        }

                    },

                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent), // Barra transparente

                    windowInsets = WindowInsets.statusBars // Ajusta padding da status bar

                )

            }

        ) { paddingValues ->

            // Conteúdo principal rolável

            LazyColumn(

                modifier = Modifier

                    .fillMaxSize()

                    .padding(paddingValues), // Aplica padding da Scaffold

                horizontalAlignment = Alignment.CenterHorizontally

            ) {

                // Seção do cabeçalho (foto, nome, botões)

                item {

                    ProfileHeader(

                        user = user,

                        isOwnProfile = isOwnProfile,

                        onEditProfileClick = onEditProfileClick

                    )

                }

                // Seção da estante (checklist)

                item {

                    ChecklistSection(

                        ratedBooks = ratedBooks,

                        onAddBookClick = onAddBookClick,

                        isOwnProfile = isOwnProfile, // Passa se é o perfil próprio

                        onDeleteBookClick = onDeleteBookClick // ⭐️ Passa o callback de exclusão

                    )

                }

                // Seção dos clubes

                item {

                    ClubsSection(

                        title = "Clubes",

                        clubs = clubs,

                        onClubClick = onClubClick

                    )

                }

            }

        }

    }

}



// --- COMPONENTES DA TELA ---



@Composable

fun ProfileHeader(

    user: User,

    isOwnProfile: Boolean,

    onEditProfileClick: () -> Unit

) {

    Column( /* ... (Sem mudanças aqui, código anterior estava ok) ... */ ) {

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

        Spacer(modifier = Modifier.height(8.dp))

        if (isOwnProfile) {

            Button(onClick = onEditProfileClick, colors = ButtonDefaults.buttonColors(containerColor = homeButtonColor)) {

                Text("Editar Perfil")

            }

        } else {

            Button(onClick = { /* TODO: Adicionar amigo */ }, enabled = false, colors = ButtonDefaults.buttonColors(containerColor = homeButtonColor)) {

                Text("Adicionar amigo")

            }

        }

        Spacer(modifier = Modifier.height(16.dp))

        if (user.aboutMe.isNotEmpty()) {

            Text(

                text = user.aboutMe,

                style = MaterialTheme.typography.bodyMedium,

                textAlign = TextAlign.Center,

                modifier = Modifier.padding(horizontal = 16.dp)

            )

        }

        Spacer(modifier = Modifier.height(24.dp))

    }

}



// Seção da Estante (Checklist)

@Composable

fun ChecklistSection(

    ratedBooks: List<ProfileRatedBook>,

    onAddBookClick: () -> Unit,

    isOwnProfile: Boolean, // ⭐️ Recebe o parâmetro

    onDeleteBookClick: (ProfileRatedBook) -> Unit // ⭐️ Recebe o callback de exclusão

) {

    Column(

        modifier = Modifier

            .fillMaxWidth()

            .padding(top = 16.dp) // Espaçamento acima da seção

    ) {

        // Cabeçalho: Título "Minha Estante" e botão "Adicionar livro" (condicional)

        Row(

            modifier = Modifier

                .fillMaxWidth()

                .padding(horizontal = 16.dp), // Padding nas laterais do cabeçalho

            horizontalArrangement = Arrangement.SpaceBetween,

            verticalAlignment = Alignment.CenterVertically

        ) {

            Text(

                text = "Minha Estante",

                style = MaterialTheme.typography.titleLarge,

                fontWeight = FontWeight.Bold

            )

            // ⭐️ Mostra o botão APENAS se isOwnProfile for true

            if (isOwnProfile) {

                Button(

                    onClick = onAddBookClick,

                    colors = ButtonDefaults.buttonColors(containerColor = homeButtonColor) // Aplicando a cor

                ) {

                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))

                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))

                    Text("Adicionar livro")

                }

            }

        }

        Spacer(modifier = Modifier.height(16.dp))



        // Conteúdo: Mensagem de estante vazia OU lista horizontal de livros

        if (ratedBooks.isEmpty()) {

            Text( // Mensagem se não houver livros

                text = "Sua estante está vazia. Adicione livros que você já leu!",

                style = MaterialTheme.typography.bodyMedium,

                modifier = Modifier

                    .fillMaxWidth()

                    .padding(horizontal = 16.dp, vertical = 8.dp),

                textAlign = TextAlign.Center

            )

        } else {

            // Lista horizontal rolável para os livros

            LazyRow(

                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), // Padding nas laterais da lista

                horizontalArrangement = Arrangement.spacedBy(12.dp) // Espaço entre os cards de livro

            ) {

                items(ratedBooks) { book ->

                    // Chama o Composable para cada item da lista

                    RatedBookItem(

                        book = book,

                        // ⭐️ Passa o lambda para deletar ESTE livro específico

                        onDeleteClick = { onDeleteBookClick(book) }

                    )

                }

            }

        }

        Spacer(modifier = Modifier.height(24.dp)) // Espaço abaixo da seção

    }

}



// Item individual da lista de livros avaliados

@OptIn(ExperimentalFoundationApi::class) // ⭐️ Habilita APIs experimentais (combinedClickable)

@Composable

fun RatedBookItem(

    book: ProfileRatedBook,

    onDeleteClick: () -> Unit, // ⭐️ Recebe o lambda para deletar

    modifier: Modifier = Modifier

) {

    Card(

        modifier = modifier

            .width(110.dp) // Largura fixa

            // ⭐️ combinedClickable detecta clique simples e clique longo

            .combinedClickable(

                onClick = { /* TODO: Ação de clique simples (ex: ver detalhes?) */ },

                onLongClick = onDeleteClick // Chama o lambda de deleção no clique longo

            ),

        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)

    ) {

        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            // Imagem da capa

            AsyncImage(

                model = book.coverUrl?.ifEmpty { null }, // Usa null se a URL for vazia

                contentDescription = book.title ?: "Capa do livro",

                modifier = Modifier

                    .height(150.dp)

                    .fillMaxWidth() ,

                contentScale = ContentScale.Crop,

                placeholder = painterResource(id = R.drawable.book_placeholder), // Usa seu PNG

                error = painterResource(id = R.drawable.book_placeholder)

            )

            Spacer(modifier = Modifier.height(4.dp))

            // Título do livro

            Text(

                text = book.title ?: "Sem Título",

                style = MaterialTheme.typography.bodySmall,

                fontWeight = FontWeight.Medium,

                maxLines = 2, // Limita a 2 linhas

                overflow = TextOverflow.Ellipsis, // Adiciona "..." se for maior

                textAlign = TextAlign.Center,

                modifier = Modifier.padding(horizontal = 4.dp)

            )

            // Nota (Estrela + número)

            Row(

                verticalAlignment = Alignment.CenterVertically,

                modifier = Modifier.padding(bottom = 6.dp, top = 2.dp)

            ) {

                Icon(

                    Icons.Filled.Star,

                    contentDescription = "Nota",

                    tint = MaterialTheme.colorScheme.primary, // Cor da estrela

                    modifier = Modifier.size(14.dp) // Tamanho um pouco menor

                )

                Spacer(modifier = Modifier.width(2.dp))

                Text(

                    // Formata a nota para 1 casa decimal (ex: "4.5")

                    text = String.format(Locale.US, "%.1f", book.rating),

                    style = MaterialTheme.typography.labelSmall,

                    fontSize = 11.sp

                )

            }

        }

    }

}



// --- Diálogo de Confirmação de Exclusão --- (Coloque aqui ou em arquivo separado)

@Composable

fun DeleteConfirmationDialog(

    bookTitle: String?,

    onDismiss: () -> Unit,

    onConfirm: () -> Unit

) {

    AlertDialog(

        onDismissRequest = onDismiss,

        title = { Text("Remover Livro") },

        text = { Text("Tem certeza que deseja remover \"${bookTitle ?: "este livro"}\" da sua estante?") },

        confirmButton = {

            TextButton(onClick = onConfirm) {

                Text("Remover", color = MaterialTheme.colorScheme.error) // Cor vermelha

            }

        },

        dismissButton = {

            TextButton(onClick = onDismiss) {

                Text("Cancelar")

            }

        }

    )

}





// --- PREVIEWS ---



@Preview(showBackground = true)

@Composable

fun ProfileScreenPreview() {

    LetrariaAppTheme {

        ProfileScreenContent(

            user = sampleUser,

            isOwnProfile = true, // Para o preview mostrar o botão "Adicionar"

            clubs = sampleClubsList,

            ratedBooks = sampleRatedBooks, // Usa a lista de exemplo

            onBackClick = {},

            onEditProfileClick = {},

            onLogoutClick = {},

            onSettingsClick = {},

            onClubClick = {},

            onAddBookClick = {},

            onDeleteBookClick = {} // Lambda vazio para o preview

        )

    }

}



@Preview

@Composable

fun RatedBookItemPreview() {

    LetrariaAppTheme {

        RatedBookItem(

            book = ProfileRatedBook(

                title = "O Nome do Vento - Livro Muito Longo Mesmo",

                coverUrl = null, // Testa o placeholder

                rating = 4.5f

            ),

            onDeleteClick = {} // Lambda vazio para o preview

        )

    }

}



@Preview

@Composable

fun DeleteConfirmationDialogPreview() { // Preview para o diálogo de exclusão

    LetrariaAppTheme {

        DeleteConfirmationDialog(bookTitle = "Nome do Livro", onDismiss = {}, onConfirm = {})

    }

}

