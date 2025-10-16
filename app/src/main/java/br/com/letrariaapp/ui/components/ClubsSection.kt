package br.com.letrariaapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.letrariaapp.data.BookClub

@Composable
fun ClubsSection(
    title: String,
    clubs: List<BookClub>,
    onClubClick: (BookClub) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(vertical = 16.dp)) {
        // O título continua o mesmo
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // --- MUDANÇA AQUI ---
        // Trocamos LazyRow por uma Column para a lista vertical
        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp), // Espaço entre os itens da lista
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Usamos um forEach para criar cada item da lista
            clubs.forEach { club ->
                ClubItemCard(club = club, onClick = { onClubClick(club) })
            }
        }
    }
}