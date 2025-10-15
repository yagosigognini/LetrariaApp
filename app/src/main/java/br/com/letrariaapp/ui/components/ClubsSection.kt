package br.com.letrariaapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.letrariaapp.data.BookClub
import br.com.letrariaapp.ui.features.home.homeTextColor

@Composable
fun ClubsSection(
    title: String,
    clubs: List<BookClub>,
    onClubClick: (BookClub) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(vertical = 16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp),
            color = homeTextColor
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(clubs) { club ->
                // Usando o ClubItemCard que também é um componente reutilizável
                ClubItemCard(club = club, onClick = { onClubClick(club) })
            }
        }
    }
}