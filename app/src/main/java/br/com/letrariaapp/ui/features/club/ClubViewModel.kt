package br.com.letrariaapp.ui.features.club

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.letrariaapp.data.BookClub
import br.com.letrariaapp.data.IndicatedBook
import br.com.letrariaapp.data.Message
import br.com.letrariaapp.data.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class ClubViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private val _club = MutableLiveData<BookClub?>()
    val club: LiveData<BookClub?> = _club

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    // ✅ NOVO: LiveData para os dados do usuário sorteado
    private val _sortedUser = MutableLiveData<User?>(null)
    val sortedUser: LiveData<User?> = _sortedUser

    private val _isLoading = MutableLiveData(true)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _accessDenied = MutableLiveData(false)
    val accessDenied: LiveData<Boolean> = _accessDenied

    private val _toastMessage = MutableLiveData<String?>(null)
    val toastMessage: LiveData<String?> = _toastMessage

    fun loadClubAndMessages(clubId: String) {
        _isLoading.value = true
        _accessDenied.value = false
        val currentUserId = auth.currentUser?.uid

        db.collection("clubs").document(clubId)
            .addSnapshotListener { document, error ->
                if (error != null) { /* ... (erro) */ return@addSnapshotListener }

                if (document != null && document.exists()) {
                    val clubData = document.toObject(BookClub::class.java)
                    _club.value = clubData

                    if (clubData != null) {
                        // ✅ VERIFICAÇÃO DE SEGURANÇA
                        if (!clubData.members.contains(currentUserId)) {
                            _accessDenied.value = true
                            _isLoading.value = false
                            _messages.value = emptyList()
                        } else {
                            // Se o usuário for membro, carrega o resto
                            _accessDenied.value = false
                            listenForMessages(clubId)

                            // ✅ BUSCA O USUÁRIO SORTEADO
                            if (clubData.currentUserForCycleId != null) {
                                fetchSortedUserData(clubData.currentUserForCycleId)
                            } else {
                                _sortedUser.value = null // Reseta se não houver ninguém
                            }
                        }
                    }
                } else { /* ... (clube não encontrado) */ }
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
        if (text.isBlank()) return
        val currentUser = auth.currentUser ?: return

        viewModelScope.launch {
            try {
                val userDoc = db.collection("users").document(currentUser.uid).get().await()
                val user = userDoc.toObject(User::class.java)

                if (user != null) {
                    val message = Message(
                        text = text,
                        senderId = user.uid,
                        senderName = user.name,
                        senderPhotoUrl = user.profilePictureUrl,
                        type = "USER" // Mensagem padrão de usuário
                    )
                    db.collection("clubs").document(clubId).collection("messages").add(message).await()
                }
            } catch (e: Exception) { Log.e("ClubViewModel", "Erro ao enviar mensagem", e) }
        }
    }

    fun drawUserForCycle() {
        viewModelScope.launch {
            val currentClub = _club.value
            val adminId = auth.currentUser?.uid
            if (currentClub == null || adminId == null) { /* ... (erro) */ return@launch }
            if (currentClub.adminId != adminId) { /* ... (erro) */ return@launch }
            if (currentClub.members.isEmpty()) { /* ... (erro) */ return@launch }

            val randomUserId = currentClub.members.random()

            // Limpa todos os campos do ciclo anterior
            val updates = mapOf(
                "currentUserForCycleId" to randomUserId,
                "indicatedBook" to null,
                "cycleEndDate" to null
            )

            try {
                db.collection("clubs").document(currentClub.id).update(updates).await()
                _toastMessage.value = "Novo usuário sorteado!"
            } catch (e: Exception) {
                _toastMessage.value = "Erro ao tentar sortear usuário."
            }
        }
    }

    private fun fetchSortedUserData(userId: String) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    _sortedUser.value = document.toObject(User::class.java)
                } else {
                    // Usuário sorteado foi deletado?
                    _sortedUser.value = null
                }
            }
    }

    fun indicateBook(clubId: String, title: String, author: String, publisher: String, cycleDays: Int) {
        viewModelScope.launch {
            val currentUserId = auth.currentUser?.uid
            if (currentUserId == null) {
                _toastMessage.value = "Usuário não autenticado."
                return@launch
            }

            // Cria o objeto do livro indicado
            val randomImageId = Random.nextInt(1, 1000)
            val book = IndicatedBook(
                title = title,
                author = author,
                publisher = publisher,
                coverUrl = "https://picsum.photos/seed/$randomImageId/400/600" // Imagem de capa padrão
            )

            // Calcula a data final do ciclo
            val cycleDaysInMillis = TimeUnit.DAYS.toMillis(cycleDays.toLong())
            val endDate = System.currentTimeMillis() + cycleDaysInMillis

            // Salva os dados no documento do Clube
            val clubUpdates = mapOf(
                "indicatedBook" to book,
                "cycleEndDate" to endDate,
                "readingCycleDays" to cycleDays // Salva a preferência de dias
            )

            try {
                db.collection("clubs").document(clubId).update(clubUpdates).await()

                // ✅ POSTA A "MENSAGEM DE SISTEMA" NO CHAT
                postIndicationMessage(clubId, book, currentUserId)

                _toastMessage.value = "Livro indicado com sucesso!"
            } catch (e: Exception) {
                Log.e("ClubViewModel", "Erro ao indicar livro", e)
                _toastMessage.value = "Erro ao salvar a indicação."
            }
        }
    }

        private suspend fun postIndicationMessage(clubId: String, book: IndicatedBook, senderId: String) {
            val userDoc = db.collection("users").document(senderId).get().await()
            val senderName = userDoc.getString("name") ?: "Admin"

            val indicationMessage = Message(
                senderId = senderId,
                senderName = senderName,
                type = "INDICATION",
                indicatedBook = book,
                text = "Nova indicação: ${book.title}" // Texto de fallback
            )
            db.collection("clubs").document(clubId).collection("messages").add(indicationMessage).await()
        }

        fun onToastMessageShown() {
            _toastMessage.value = null
        }

    }

