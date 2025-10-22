package br.com.letrariaapp.ui.features.club

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.letrariaapp.data.BookClub
import br.com.letrariaapp.data.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// "Molde" para a UI de Solicitações
data class JoinRequest(
    val userId: String,
    val user: User
)

class AdminViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private val _club = MutableLiveData<BookClub?>()
    val club: LiveData<BookClub?> = _club

    private val _requests = MutableLiveData<List<JoinRequest>>()
    val requests: LiveData<List<JoinRequest>> = _requests

    // ✅ NOVO: LiveData para a lista de membros (para a aba "Membros")
    private val _members = MutableLiveData<List<User>>()
    val members: LiveData<List<User>> = _members

    private val _isLoadingRequests = MutableLiveData(false)
    val isLoadingRequests: LiveData<Boolean> = _isLoadingRequests

    private val _isLoadingMembers = MutableLiveData(false)
    val isLoadingMembers: LiveData<Boolean> = _isLoadingMembers

    private lateinit var currentClubId: String

    fun loadAdminData(clubId: String) {
        currentClubId = clubId
        _isLoadingRequests.value = true
        _isLoadingMembers.value = true

        // "Ouve" o documento do clube em tempo real
        db.collection("clubs").document(clubId)
            .addSnapshotListener { clubSnapshot, error ->
                if (error != null || clubSnapshot == null) {
                    Log.e("AdminViewModel", "Erro ao carregar clube", error)
                    _isLoadingRequests.value = false
                    _isLoadingMembers.value = false
                    return@addSnapshotListener
                }

                val clubData = clubSnapshot.toObject(BookClub::class.java)
                _club.value = clubData

                if (clubData == null) {
                    _isLoadingRequests.value = false
                    _isLoadingMembers.value = false
                    return@addSnapshotListener
                }

                // 1. Processa a lista de SOLICITAÇÕES
                val requestIds = clubData.joinRequests
                if (requestIds.isEmpty()) {
                    _requests.value = emptyList()
                    _isLoadingRequests.value = false
                } else {
                    fetchUsersFromIds(requestIds, isRequest = true)
                }

                // 2. Processa a lista de MEMBROS
                val memberIds = clubData.members
                if (memberIds.isEmpty()) {
                    _members.value = emptyList()
                    _isLoadingMembers.value = false
                } else {
                    fetchUsersFromIds(memberIds, isRequest = false)
                }
            }
    }

    private fun fetchUsersFromIds(userIds: List<String>, isRequest: Boolean) {
        viewModelScope.launch {
            try {
                val usersSnapshot = db.collection("users").whereIn("uid", userIds).get().await()
                val users = usersSnapshot.toObjects(User::class.java)

                if (isRequest) {
                    // Mapeia para a lista de solicitações
                    val joinRequests = userIds.mapNotNull { id ->
                        users.find { it.uid == id }?.let { user ->
                            JoinRequest(userId = id, user = user)
                        }
                    }
                    _requests.value = joinRequests
                    _isLoadingRequests.value = false
                } else {
                    // Mapeia para a lista de membros
                    _members.value = users
                    _isLoadingMembers.value = false
                }
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Erro ao buscar perfis de usuários", e)
                if (isRequest) _isLoadingRequests.value = false else _isLoadingMembers.value = false
            }
        }
    }

    fun approveRequest(userId: String) {
        viewModelScope.launch {
            try {
                val clubRef = db.collection("clubs").document(currentClubId)
                db.runTransaction { transaction ->
                    val club = transaction.get(clubRef).toObject(BookClub::class.java)
                    if (club != null && club.members.size >= club.maxMembers) {
                        throw Exception("O clube está lotado.")
                    }

                    transaction.update(clubRef, "joinRequests", FieldValue.arrayRemove(userId))
                    transaction.update(clubRef, "members", FieldValue.arrayUnion(userId))
                    null
                }.await()
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Erro ao aprovar usuário", e)
                // TODO: Enviar Toast de erro para a UI
            }
        }
    }

    fun denyRequest(userId: String) {
        // ... (esta função continua a mesma)
    }

    // ✅ NOVA FUNÇÃO: Para expulsar um membro
    fun kickMember(userId: String) {
        viewModelScope.launch {
            try {
                db.collection("clubs").document(currentClubId)
                    .update("members", FieldValue.arrayRemove(userId))
                    .await()
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Erro ao expulsar membro", e)
            }
        }
    }
}