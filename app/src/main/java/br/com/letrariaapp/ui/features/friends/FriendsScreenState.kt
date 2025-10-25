package br.com.letrariaapp.ui.features.friends

import br.com.letrariaapp.data.Friend
import br.com.letrariaapp.data.FriendRequest

// Define tudo que a tela "Meus Amigos" pode exibir
data class FriendsScreenState(
    val isLoading: Boolean = true, // Começa carregando
    val receivedRequests: List<FriendRequest> = emptyList(), // Lista de pedidos recebidos
    val sentRequests: List<FriendRequest> = emptyList(), // Lista de pedidos enviados
    val friends: List<Friend> = emptyList(), // Lista de amigos confirmados
    val errorMessage: String? = null // Mensagem de erro, se houver
)