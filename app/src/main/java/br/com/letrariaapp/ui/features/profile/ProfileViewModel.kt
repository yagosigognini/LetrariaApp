package br.com.letrariaapp.ui.features.profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.letrariaapp.data.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

enum class UpdateStatus { IDLE, LOADING, SUCCESS, ERROR }

class ProfileViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val storage = Firebase.storage

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _isOwnProfile = MutableLiveData<Boolean>()
    val isOwnProfile: LiveData<Boolean> = _isOwnProfile

    private val _updateStatus = MutableLiveData<UpdateStatus>(UpdateStatus.IDLE)
    val updateStatus: LiveData<UpdateStatus> = _updateStatus

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    fun loadUserProfile(userId: String?) {
        val currentUserId = auth.currentUser?.uid
        Log.d("ProfileViewModel", "ID do usuário logado (auth.currentUser.uid): $currentUserId")

        if (userId == null && currentUserId == null) {
            Log.e("ProfileViewModel", "ERRO CRÍTICO: userId e currentUserId são nulos.")
            _user.value = null
            return
        }
        val idToFetch = userId ?: currentUserId!!
        _isOwnProfile.value = (idToFetch == currentUserId)

        Log.d("ProfileViewModel", "Iniciando busca no Firestore para o documento com ID: $idToFetch")
        db.collection("users").document(idToFetch).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    Log.d("ProfileViewModel", "SUCESSO! Documento encontrado.")
                    try {
                        val userObject = document.toObject(User::class.java)
                        _user.value = userObject
                        Log.d("ProfileViewModel", "Usuário convertido e carregado: ${userObject?.name}")
                    } catch (e: Exception) {
                        Log.e("ProfileViewModel", "FALHA na conversão do documento para objeto User:", e)
                        _user.value = null
                    }
                } else {
                    Log.w("ProfileViewModel", "AVISO: Documento NÃO encontrado para o ID: $idToFetch")
                    _user.value = null
                }
            }
            .addOnFailureListener { exception ->
                Log.e("ProfileViewModel", "FALHA ao buscar usuário:", exception)
                _user.value = null
            }
    }

    // --- FUNÇÃO DE ATUALIZAÇÃO AJUSTADA ---
    // A função agora ignora 'newImageUri' e salva apenas o texto
    fun updateUserProfile(name: String, aboutMe: String, newImageUri: Uri?) {
        viewModelScope.launch {
            _updateStatus.value = UpdateStatus.LOADING
            Log.d("ProfileViewModel", "Iniciando atualização do perfil (APENAS TEXTO)...")

            val uid = auth.currentUser?.uid ?: run {
                _errorMessage.value = "Usuário não autenticado."
                _updateStatus.value = UpdateStatus.ERROR
                return@launch
            }

            try {
                // A lógica de upload foi removida por enquanto.
                // Chamamos diretamente a atualização do documento com a URL de imagem como nula.
                updateUserDocument(uid, name, aboutMe, null)

            } catch (e: Exception) {
                _updateStatus.value = UpdateStatus.ERROR
                Log.e("ProfileViewModel", "Uma falha ocorreu durante a atualização do texto:", e)
            }
        }
    }

    private suspend fun updateUserDocument(uid: String, name: String, aboutMe: String, imageUrl: String?) {
        val userRef = db.collection("users").document(uid)
        val updates = mutableMapOf<String, Any>()
        updates["name"] = name
        updates["aboutMe"] = aboutMe

        // Se uma nova URL de imagem for fornecida (o que não vai acontecer por enquanto),
        // ela será adicionada. Caso contrário, o campo da foto não é alterado.
        if (imageUrl != null) {
            updates["profilePictureUrl"] = imageUrl
        }

        Log.d("ProfileViewModel", "Atualizando documento no Firestore...")
        userRef.update(updates).await()

        Log.d("ProfileViewModel", "Documento atualizado com sucesso.")
        _updateStatus.value = UpdateStatus.SUCCESS
        loadUserProfile(uid)
    }

    fun resetUpdateStatus() {
        _updateStatus.value = UpdateStatus.IDLE
        _errorMessage.value = null
    }
}