package br.com.letrariaapp.ui.features.searchuser

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.letrariaapp.data.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SearchUserViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    // StateFlow para expor o estado da busca para a UI
    private val _searchState = MutableStateFlow<SearchUserState>(SearchUserState.Idle)
    val searchState: StateFlow<SearchUserState> = _searchState

    /**
     * Busca usuários no Firestore pelo nome.
     * @param query O nome (ou parte do nome) a ser buscado.
     */
    fun searchUsers(query: String) {
        val trimmedQuery = query.trim() // Remove espaços extras
        if (trimmedQuery.isBlank()) {
            _searchState.value = SearchUserState.Idle // Não busca se a query estiver vazia
            return
        }

        _searchState.value = SearchUserState.Loading // Define estado como carregando
        val currentUserId = auth.currentUser?.uid // Pega o ID do usuário atual para excluí-lo dos resultados

        viewModelScope.launch {
            try {
                // Query no Firestore: busca usuários cujo 'name' começa com a 'trimmedQuery'
                // O caractere '\uf8ff' é um truque comum para pegar prefixos em queries do Firestore
                val result = db.collection("users")
                    .whereGreaterThanOrEqualTo("name", trimmedQuery)
                    .whereLessThanOrEqualTo("name", trimmedQuery + '\uf8ff')
                    .orderBy("name") // Ordena por nome
                    .limit(20) // Limita a 20 resultados
                    .get()
                    .await() // Espera o resultado

                // Converte os documentos para objetos User e filtra o usuário atual
                val users = result.toObjects(User::class.java).filter { it.uid != currentUserId }

                // Define o estado com base nos resultados
                if (users.isEmpty()) {
                    _searchState.value = SearchUserState.NoResults
                } else {
                    _searchState.value = SearchUserState.Success(users)
                }
                Log.d("SearchUserVM", "Busca por '$trimmedQuery' retornou ${users.size} usuários.")

            } catch (e: Exception) {
                // Trata erros (rede, permissão do Firestore, etc.)
                Log.e("SearchUserVM", "Erro ao buscar usuários por '$trimmedQuery'", e)
                _searchState.value = SearchUserState.Error("Erro ao buscar usuários: ${e.message}")
            }
        }
    }

    /** Reseta o estado da busca para Idle. */
    fun clearSearch() {
        _searchState.value = SearchUserState.Idle
    }
}