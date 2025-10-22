package br.com.letrariaapp.ui.features.profile

import android.net.Uri
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
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

enum class UpdateStatus { IDLE, LOADING, SUCCESS, ERROR }

class ProfileViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val storage = Firebase.storage

    private val _user = MutableLiveData<User?>(null)
    val user: LiveData<User?> = _user

    private val _clubs = MutableLiveData<List<BookClub>>()
    val clubs: LiveData<List<BookClub>> = _clubs

    private val _isOwnProfile = MutableLiveData<Boolean>()
    val isOwnProfile: LiveData<Boolean> = _isOwnProfile

    private val _updateStatus = MutableLiveData<UpdateStatus>(UpdateStatus.IDLE)
    val updateStatus: LiveData<UpdateStatus> = _updateStatus

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    fun loadUserProfile(userId: String?) {
        val currentUserId = auth.currentUser?.uid // ID de quem está VENDO
        val idToFetch = userId ?: currentUserId // ID do perfil a ser MOSTRADO

        if (idToFetch == null) {
            Log.e("ProfileViewModel", "ERRO CRÍTICO: Não foi possível determinar o usuário.")
            _user.value = null
            _clubs.value = emptyList()
            return
        }

        _isOwnProfile.value = (idToFetch == currentUserId)

        // 1. Busca os dados do usuário do perfil
        db.collection("users").document(idToFetch).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val userObject = document.toObject(User::class.java)
                    _user.value = userObject
                    if (userObject != null) {
                        // 2. Passa o ID do dono do perfil E o ID de quem está vendo
                        fetchUserClubs(userObject.uid, currentUserId)
                    }
                } else {
                    _user.value = null
                    Log.w("ProfileViewModel", "Documento não encontrado para o ID: $idToFetch")
                }
            }
            .addOnFailureListener {
                _user.value = null
                Log.e("ProfileViewModel", "Falha ao buscar usuário", it)
            }
    }

    // A sua lógica de privacidade está perfeita e foi mantida
    private fun fetchUserClubs(profileOwnerId: String, viewerId: String?) {
        db.collection("clubs").whereArrayContains("members", profileOwnerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("ProfileViewModel", "Erro ao buscar clubes.", error)
                    _clubs.value = emptyList()
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val allClubs = snapshot.toObjects(BookClub::class.java)

                    val filteredClubs = allClubs.filter { club ->
                        club.isPublic || club.members.contains(viewerId)
                    }
                    _clubs.value = filteredClubs
                }
            }
    }

    // ✅ --- FUNÇÃO DE ATUALIZAÇÃO CORRIGIDA ---
    // Esta é a versão que também faz o upload da foto.
    fun updateUserProfile(name: String, aboutMe: String, newImageUri: Uri?) {
        viewModelScope.launch {
            _updateStatus.value = UpdateStatus.LOADING
            Log.d("ProfileViewModel", "Iniciando atualização do perfil via coroutine...")

            val uid = auth.currentUser?.uid ?: run {
                _errorMessage.value = "Usuário não autenticado."
                _updateStatus.value = UpdateStatus.ERROR
                return@launch
            }

            try {
                // Lógica de upload de imagem (re-ativada)
                val imageUrl = if (newImageUri != null) {
                    val imageRef = storage.reference.child("profile_pictures/$uid.jpg")
                    Log.d("ProfileViewModel", "Fazendo upload de nova imagem...")

                    imageRef.putFile(newImageUri).await() // A mágica do await()
                    Log.d("ProfileViewModel", "Upload bem-sucedido. Obtendo URL de download...")

                    val downloadUrl = imageRef.downloadUrl.await().toString()
                    Log.d("ProfileViewModel", "URL obtida: $downloadUrl")
                    downloadUrl
                } else {
                    // Se não há imagem nova, mantém a URL antiga que já estava no perfil
                    user.value?.profilePictureUrl
                }

                updateUserDocument(uid, name, aboutMe, imageUrl)

            } catch (e: Exception) {
                _updateStatus.value = UpdateStatus.ERROR
                _errorMessage.value = "Falha ao atualizar o perfil."
                Log.e("ProfileViewModel", "Uma falha ocorreu durante a atualização:", e)
            }
        }
    }

    private suspend fun updateUserDocument(uid: String, name: String, aboutMe: String, imageUrl: String?) {
        val userRef = db.collection("users").document(uid)
        val updates = mutableMapOf<String, Any>()
        updates["name"] = name
        updates["aboutMe"] = aboutMe

        // Só adiciona a URL da foto ao mapa se ela não for nula
        if (imageUrl != null) {
            updates["profilePictureUrl"] = imageUrl
        }

        Log.d("ProfileViewModel", "Atualizando documento no Firestore com os dados: $updates")
        userRef.update(updates).await()

        Log.d("ProfileViewModel", "Documento atualizado com sucesso.")
        _updateStatus.value = UpdateStatus.SUCCESS
        // Recarrega o perfil para atualizar a UI imediatamente
        loadUserProfile(uid)
    }

    fun resetUpdateStatus() {
        _updateStatus.value = UpdateStatus.IDLE
        _errorMessage.value = null
    }
}