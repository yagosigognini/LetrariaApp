package br.com.letrariaapp.ui.features.club

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.letrariaapp.data.BookClub
import br.com.letrariaapp.data.Message
import br.com.letrariaapp.data.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ClubViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private val _club = MutableLiveData<BookClub?>()
    val club: LiveData<BookClub?> = _club

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    private val _isLoading = MutableLiveData(true)
    val isLoading: LiveData<Boolean> = _isLoading

    // ✅ NOVO: LiveData para bloquear acesso
    private val _accessDenied = MutableLiveData(false)
    val accessDenied: LiveData<Boolean> = _accessDenied

    private val _toastMessage = MutableLiveData<String?>(null)
    val toastMessage: LiveData<String?> = _toastMessage

    fun loadClubAndMessages(clubId: String) {
        _isLoading.value = true
        _accessDenied.value = false
        val currentUserId = auth.currentUser?.uid

        // 1. Ouve os dados do clube em tempo real
        db.collection("clubs").document(clubId)
            .addSnapshotListener { document, error ->
                if (error != null) {
                    Log.e("ClubViewModel", "Erro ao ouvir detalhes do clube", error)
                    _isLoading.value = false
                    return@addSnapshotListener
                }
                if (document != null && document.exists()) {
                    val clubData = document.toObject(BookClub::class.java)
                    _club.value = clubData

                    // ✅ VERIFICAÇÃO DE SEGURANÇA
                    if (clubData != null && !clubData.members.contains(currentUserId)) {
                        Log.w("ClubViewModel", "ACESSO NEGADO! Usuário $currentUserId não é membro do clube $clubId")
                        _accessDenied.value = true
                        _isLoading.value = false
                        _messages.value = emptyList()
                    } else {
                        // Se o usuário for membro, carrega as mensagens
                        _accessDenied.value = false
                        listenForMessages(clubId)
                    }
                } else {
                    Log.w("ClubViewModel", "Clube não encontrado com o ID: $clubId")
                    _isLoading.value = false
                }
            }
    }

    // ✅ Lógica de ouvir mensagens movida para função separada
    private fun listenForMessages(clubId: String) {
        db.collection("clubs").document(clubId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("ClubViewModel", "Erro ao ouvir mensagens.", error)
                    _isLoading.value = false
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    _messages.value = snapshot.toObjects(Message::class.java)
                }
                _isLoading.value = false
            }
    }

    fun sendMessage(text: String, clubId: String) {
        if (text.isBlank()) {
            return
        }

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e("ClubViewModel", "Usuário não logado, não pode enviar mensagem.")
            return
        }

        // Para enviar a mensagem, primeiro buscamos os dados do nosso usuário
        // para podermos salvar o nome e a foto junto com a mensagem.
        viewModelScope.launch {
            try {
                val userDoc = db.collection("users").document(currentUser.uid).get().await()
                val user = userDoc.toObject(User::class.java)

                if (user != null) {
                    val message = Message(
                        text = text,
                        senderId = user.uid,
                        senderName = user.name,
                        senderPhotoUrl = user.profilePictureUrl
                    )

                    // Adiciona a nova mensagem na subcoleção do clube
                    db.collection("clubs").document(clubId).collection("messages").add(message).await()
                    Log.d("ClubViewModel", "Mensagem enviada com sucesso!")
                }
            } catch (e: Exception) {
                Log.e("ClubViewModel", "Erro ao enviar mensagem", e)
            }
        }
    }
    fun drawUserForCycle() {
        viewModelScope.launch {
            val currentClub = _club.value
            val adminId = auth.currentUser?.uid

            if (currentClub == null || adminId == null) {
                _toastMessage.value = "Erro: Clube ou usuário não encontrado."
                return@launch
            }

            // Verifica se o usuário atual é o admin do clube
            if (currentClub.adminId != adminId) {
                _toastMessage.value = "Apenas o admin pode sortear um usuário."
                return@launch
            }

            if (currentClub.members.isEmpty()) {
                _toastMessage.value = "Não há membros no clube para sortear."
                return@launch
            }

            // Sorteia um ID de usuário aleatório da lista de membros
            val randomUserId = currentClub.members.random()

            // Prepara os dados para atualizar no Firestore
            val updates = mapOf(
                "currentUserForCycleId" to randomUserId,
                "indicatedBookTitle" to null,
                "cycleEndDate" to null
            )

            try {
                // Atualiza o documento do clube no banco de dados
                db.collection("clubs").document(currentClub.id).update(updates).await()
                _toastMessage.value = "Novo usuário sorteado!"
                Log.d("ClubViewModel", "Usuário $randomUserId sorteado para o ciclo.")
            } catch (e: Exception) {
                Log.e("ClubViewModel", "Erro ao sortear usuário", e)
                _toastMessage.value = "Erro ao tentar sortear usuário."
            }
        }
    }

    fun onToastMessageShown() {
        _toastMessage.value = null
    }
}
