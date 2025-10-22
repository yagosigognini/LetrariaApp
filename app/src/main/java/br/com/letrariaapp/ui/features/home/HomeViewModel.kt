package br.com.letrariaapp.ui.features.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.letrariaapp.data.BookClub
import br.com.letrariaapp.data.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private val _user = MutableLiveData<User?>(null)
    val user: LiveData<User?> = _user

    // ✅ NOVO: LiveData para a lista de clubes do usuário
    private val _clubs = MutableLiveData<List<BookClub>>()
    val clubs: LiveData<List<BookClub>> = _clubs

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid
            if (uid == null) {
                Log.e("HomeViewModel", "Nenhum usuário logado.")
                _user.value = null
                _clubs.value = emptyList() // Garante que a lista fique vazia
                return@launch
            }

            // Busca os dados do usuário (como já fazíamos)
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        _user.value = document.toObject(User::class.java)
                        // ✅ ASSIM QUE O USUÁRIO É CARREGADO, BUSCAMOS SEUS CLUBES
                        fetchUserClubs(uid)
                    }
                }
        }
    }

    // ✅ NOVA FUNÇÃO
    private fun fetchUserClubs(userId: String) {
        // Esta é uma consulta poderosa do Firestore:
        // Busque na coleção "clubs" todos os documentos onde o array "members"
        // contenha o ID do nosso usuário.
        db.collection("clubs").whereArrayContains("members", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("HomeViewModel", "Erro ao buscar clubes do usuário.", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val clubList = snapshot.toObjects(BookClub::class.java)
                    _clubs.value = clubList
                    Log.d("HomeViewModel", "Clubes do usuário carregados: ${clubList.size}")
                }
            }
    }
}