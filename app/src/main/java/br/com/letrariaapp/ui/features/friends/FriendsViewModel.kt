package br.com.letrariaapp.ui.features.friends

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.letrariaapp.data.Friend
import br.com.letrariaapp.data.FriendRequest
import br.com.letrariaapp.data.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FriendsViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val currentUserId = auth.currentUser?.uid

    // Listeners do Firestore para limpar no final
    private var receivedListener: ListenerRegistration? = null
    private var sentListener: ListenerRegistration? = null
    private var friendsListener: ListenerRegistration? = null

    // StateFlow para gerenciar o estado da UI
    private val _state = MutableStateFlow(FriendsScreenState())
    val state: StateFlow<FriendsScreenState> = _state.asStateFlow()

    // ⬇️ --- NOVO ESTADO PARA DIÁLOGO DE CONFIRMAÇÃO --- ⬇️
    private val _friendToRemove = MutableStateFlow<Friend?>(null)
    val friendToRemove: StateFlow<Friend?> = _friendToRemove.asStateFlow()
    // ⬆️ --- FIM DO NOVO ESTADO --- ⬆️

    // Bloco inicial: Carrega todos os dados
    init {
        loadAllFriendsData()
    }

    private fun loadAllFriendsData() {
        if (currentUserId == null) {
            _state.update { it.copy(isLoading = false, errorMessage = "Usuário não autenticado.") }
            return
        }

        listenForReceivedRequests(currentUserId)
        listenForSentRequests(currentUserId)
        listenForFriends(currentUserId)
    }

    // 1. Ouve pedidos recebidos
    private fun listenForReceivedRequests(myUserId: String) {
        receivedListener?.remove()
        receivedListener = db.collection("friend_requests")
            .whereEqualTo("receiverId", myUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FriendsVM", "Erro ao ouvir pedidos recebidos", error)
                    _state.update { it.copy(errorMessage = "Erro ao carregar pedidos.") }
                    return@addSnapshotListener
                }
                val requests = snapshot?.toObjects(FriendRequest::class.java) ?: emptyList()
                _state.update { it.copy(receivedRequests = requests) }
            }
    }

    // 2. Ouve pedidos enviados
    private fun listenForSentRequests(myUserId: String) {
        sentListener?.remove()
        sentListener = db.collection("friend_requests")
            .whereEqualTo("senderId", myUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FriendsVM", "Erro ao ouvir pedidos enviados", error)
                    return@addSnapshotListener
                }
                val requests = snapshot?.toObjects(FriendRequest::class.java) ?: emptyList()
                _state.update { it.copy(sentRequests = requests) }
            }
    }

    // 3. Ouve amigos confirmados
    private fun listenForFriends(myUserId: String) {
        friendsListener?.remove()
        friendsListener = db.collection("users").document(myUserId).collection("friends")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FriendsVM", "Erro ao ouvir lista de amigos", error)
                    _state.update { it.copy(isLoading = false, errorMessage = "Erro ao carregar amigos.") }
                    return@addSnapshotListener
                }
                val friendsList = snapshot?.toObjects(Friend::class.java) ?: emptyList()
                _state.update { it.copy(isLoading = false, friends = friendsList) }
            }
    }

    // --- AÇÕES ---

    /** Recusa um pedido de amizade (simplesmente deleta o pedido) */
    fun declineFriendRequest(request: FriendRequest) {
        if (currentUserId == null) return
        viewModelScope.launch {
            try {
                // Encontra o documento do pedido e deleta
                val requestDoc = db.collection("friend_requests")
                    .whereEqualTo("senderId", request.senderId)
                    .whereEqualTo("receiverId", request.receiverId)
                    .limit(1).get().await()

                if (!requestDoc.isEmpty) {
                    requestDoc.documents[0].reference.delete().await()
                    Log.d("FriendsVM", "Pedido de ${request.senderName} recusado.")
                }
            } catch (e: Exception) {
                Log.e("FriendsVM", "Erro ao recusar pedido", e)
                _state.update { it.copy(errorMessage = "Erro ao recusar pedido.") }
            }
        }
    }

    /** Aceita um pedido de amizade */
    fun acceptFriendRequest(request: FriendRequest) {
        val myUserId = currentUserId
        val senderId = request.senderId
        if (myUserId == null) return

        viewModelScope.launch {
            try {
                // 1. Precisamos dos dados do usuário atual (o receptor)
                val myUserDoc = db.collection("users").document(myUserId).get().await()
                val myUser = myUserDoc.toObject<User>()
                if (myUser == null) {
                    _state.update { it.copy(errorMessage = "Erro: Seus dados de usuário não foram encontrados.") }
                    return@launch
                }

                // 2. Prepara os objetos 'Friend' para ambos
                val friendForMe = Friend(
                    uid = request.senderId,
                    name = request.senderName,
                    profilePictureUrl = request.senderPhotoUrl
                )
                val friendForSender = Friend(
                    uid = myUser.uid,
                    name = myUser.name,
                    profilePictureUrl = myUser.profilePictureUrl
                )

                // 3. Referências dos documentos no Firestore
                val myFriendDocRef = db.collection("users").document(myUserId).collection("friends").document(senderId)
                val senderFriendDocRef = db.collection("users").document(senderId).collection("friends").document(myUserId)
                val myUserDocRef = db.collection("users").document(myUserId)
                val senderUserDocRef = db.collection("users").document(senderId)

                // 4. Referência do pedido a ser deletado
                val requestDocRef = db.collection("friend_requests")
                    .whereEqualTo("senderId", senderId)
                    .whereEqualTo("receiverId", myUserId)
                    .limit(1).get().await().documents.first().reference

                // 5. Executa tudo em uma transação
                db.runTransaction { transaction ->
                    transaction.delete(requestDocRef)
                    transaction.set(myFriendDocRef, friendForMe)
                    transaction.set(senderFriendDocRef, friendForSender)
                    transaction.update(myUserDocRef, "friendCount", FieldValue.increment(1))
                    transaction.update(senderUserDocRef, "friendCount", FieldValue.increment(1))
                    null
                }.await()

                Log.d("FriendsVM", "Amizade com ${request.senderName} aceita!")

            } catch (e: Exception) {
                Log.e("FriendsVM", "Erro ao aceitar pedido", e)
                _state.update { it.copy(errorMessage = "Erro ao aceitar pedido.") }
            }
        }
    }

    /** Cancela um pedido de amizade enviado */
    fun cancelSentRequest(request: FriendRequest) {
        if (currentUserId == null || currentUserId != request.senderId) return
        viewModelScope.launch {
            try {
                // Encontra o documento do pedido e deleta
                val requestDoc = db.collection("friend_requests")
                    .whereEqualTo("senderId", request.senderId)
                    .whereEqualTo("receiverId", request.receiverId)
                    .limit(1).get().await()

                if (!requestDoc.isEmpty) {
                    requestDoc.documents[0].reference.delete().await()
                    Log.d("FriendsVM", "Pedido enviado para ${request.receiverName} cancelado.")
                }
            } catch (e: Exception) {
                Log.e("FriendsVM", "Erro ao cancelar pedido", e)
                _state.update { it.copy(errorMessage = "Erro ao cancelar pedido.") }
            }
        }
    }

    // ⬇️ --- NOVAS FUNÇÕES PARA CONFIRMAR REMOÇÃO --- ⬇️

    /** Chamado pela UI (botão X) para iniciar a remoção */
    fun requestRemoveFriend(friend: Friend) {
        _friendToRemove.value = friend
    }

    /** Chamado pelo diálogo para cancelar a remoção */
    fun cancelRemoveFriend() {
        _friendToRemove.value = null
    }
    // ⬆️ --- FIM DAS NOVAS FUNÇÕES --- ⬆️

    /** Remove um amigo (agora chamado pelo diálogo de confirmação) */
    fun removeFriend(friend: Friend) {
        val myUserId = currentUserId
        val friendId = friend.uid
        if (myUserId == null) return

        viewModelScope.launch {
            try {
                // Usamos um Lote (Batch)
                val batch = db.batch()

                val myFriendDocRef = db.collection("users").document(myUserId).collection("friends").document(friendId)
                val friendUserDocRef = db.collection("users").document(friendId).collection("friends").document(myUserId)
                val myUserDocRef = db.collection("users").document(myUserId)
                val friendUserDocRefMain = db.collection("users").document(friendId)

                batch.delete(myFriendDocRef)
                batch.delete(friendUserDocRef)
                batch.update(myUserDocRef, "friendCount", FieldValue.increment(-1))
                batch.update(friendUserDocRefMain, "friendCount", FieldValue.increment(-1))

                batch.commit().await()
                Log.d("FriendsVM", "Amizade com ${friend.name} removida.")

            } catch (e: Exception) {
                Log.e("FriendsVM", "Erro ao remover amigo", e)
                _state.update { it.copy(errorMessage = "Erro ao remover amigo.") }
            } finally {
                // ✅ Limpa o estado de confirmação, quer tenha dado certo ou erro
                _friendToRemove.value = null
            }
        }
    }

    // Limpa todos os listeners quando o ViewModel é destruído
    override fun onCleared() {
        super.onCleared()
        receivedListener?.remove()
        sentListener?.remove()
        friendsListener?.remove()
        Log.d("FriendsVM", "Listeners de amizade removidos.")
    }
}