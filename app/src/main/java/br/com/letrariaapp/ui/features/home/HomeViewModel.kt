package br.com.letrariaapp.ui.features.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    // Chama a função para carregar os dados assim que o ViewModel for criado
    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid
            if (uid == null) {
                Log.e("HomeViewModel", "Nenhum usuário logado para carregar os dados.")
                _user.value = null
                return@launch
            }

            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        _user.value = document.toObject(User::class.java)
                        Log.d("HomeViewModel", "Usuário da Home carregado: ${_user.value?.name}")
                    } else {
                        _user.value = null
                        Log.w("HomeViewModel", "Documento do usuário não encontrado no Firestore.")
                    }
                }
                .addOnFailureListener { exception ->
                    _user.value = null
                    Log.e("HomeViewModel", "Falha ao buscar usuário para a Home:", exception)
                }
        }
    }
}