package br.com.letrariaapp.ui.features.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState // ✅ IMPORT ADICIONADO
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.letrariaapp.R
import br.com.letrariaapp.data.BookClub
import br.com.letrariaapp.data.sampleClubsList
import br.com.letrariaapp.ui.components.AppBackground
import br.com.letrariaapp.ui.components.ClubItemCard
import br.com.letrariaapp.ui.theme.LetrariaAppTheme

@Composable
fun SearchClubScreen(
    viewModel: SearchClubViewModel = viewModel(),
    onBackClick: () -> Unit,
    onClubClick: (BookClub) -> Unit, // ✅ PARÂMETRO ADICIONADO
    detailsViewModel: ClubDetailsViewModel = viewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedClubId by remember { mutableStateOf<String?>(null) }

    val allClubs by viewModel.clubs.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)

    val filteredClubs = allClubs.filter { it.name.contains(searchQuery, ignoreCase = true) }

    selectedClubId?.let { id ->
        ClubDetailsDialog(
            clubId = id,
            viewModel = detailsViewModel,
            onDismiss = { selectedClubId = null },
            onJoinClick = { detailsViewModel.joinClub() }
        )
    }

    SearchClubScreenContent(
        searchQuery = searchQuery,
        onSearchQueryChange = { searchQuery = it },
        clubs = filteredClubs,
        isLoading = isLoading,
        onBackClick = onBackClick,
        onClubClick = { club -> selectedClubId = club.id } // Usamos o clique para mostrar o diálogo
        // A navegação para o chat será feita daqui
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchClubScreenContent(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    clubs: List<BookClub>,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onClubClick: (BookClub) -> Unit // ✅ PARÂMETRO ADICIONADO
) {
    AppBackground(backgroundResId = R.drawable.background) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Procurar Clube") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { Text("Insira o nome ou o código de acesso") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Ícone de busca") },
                    singleLine = true
                )

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(clubs) { club ->
                            ClubItemCard(
                                club = club,
                                onClubClick = onClubClick // ✅ CONEXÃO FEITA AQUI
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchClubScreenPreview() {
    LetrariaAppTheme {
        SearchClubScreenContent(
            searchQuery = "",
            onSearchQueryChange = {},
            clubs = sampleClubsList,
            isLoading = false,
            onBackClick = {},
            onClubClick = {} // ✅ ADICIONADO AO PREVIEW
        )
    }
}