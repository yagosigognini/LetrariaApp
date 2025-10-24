package br.com.letrariaapp.ui.features.profile



import android.net.Uri

import android.util.Log

import androidx.compose.runtime.State

import androidx.compose.runtime.mutableStateOf

import androidx.lifecycle.LiveData

import androidx.lifecycle.MutableLiveData

import androidx.lifecycle.ViewModel

import androidx.lifecycle.viewModelScope

import br.com.letrariaapp.data.BookClub

import br.com.letrariaapp.data.BookItem

import br.com.letrariaapp.data.ProfileRatedBook

import br.com.letrariaapp.data.User

import br.com.letrariaapp.data.getBestAvailableImageUrl

import com.google.firebase.auth.ktx.auth

import com.google.firebase.firestore.FieldValue

import com.google.firebase.firestore.ListenerRegistration

import com.google.firebase.firestore.Query

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



    // Listeners

    private var clubsListener: ListenerRegistration? = null

    private var ratedBooksListener: ListenerRegistration? = null



    // LiveData e State para a UI

    private val _user = MutableLiveData<User?>(null)

    val user: LiveData<User?> = _user



    private val _clubs = MutableLiveData<List<BookClub>>(emptyList())

    val clubs: LiveData<List<BookClub>> = _clubs



    private val _ratedBooks = MutableLiveData<List<ProfileRatedBook>>(emptyList())

    val ratedBooks: LiveData<List<ProfileRatedBook>> = _ratedBooks



    private val _isOwnProfile = MutableLiveData<Boolean>()

    val isOwnProfile: LiveData<Boolean> = _isOwnProfile



    private val _updateStatus = MutableLiveData<UpdateStatus>(UpdateStatus.IDLE)

    val updateStatus: LiveData<UpdateStatus> = _updateStatus



    private val _errorMessage = MutableLiveData<String?>(null)

    val errorMessage: LiveData<String?> = _errorMessage



    // ⬇️ --- ADICIONADO: LiveData para mensagens Toast --- ⬇️

    private val _toastMessage = MutableLiveData<String?>(null)

    val toastMessage: LiveData<String?> = _toastMessage

    // ⬆️ --- FIM DA ADIÇÃO --- ⬆️



    // State para diálogo de avaliação

    private val _bookToRate = mutableStateOf<BookItem?>(null)

    val bookToRate: State<BookItem?> = _bookToRate



    // State para diálogo de exclusão

    private val _bookToDelete = mutableStateOf<ProfileRatedBook?>(null)

    val bookToDelete: State<ProfileRatedBook?> = _bookToDelete





    // --- Funções ---



    // ... (loadUserProfile, fetchRatedBooks, fetchUserClubs - sem mudanças na lógica interna, mas limpam listeners) ...

    fun loadUserProfile(userId: String?) {

        val currentUserId = auth.currentUser?.uid

        val idToFetch = userId ?: currentUserId



        if (idToFetch == null) {

            Log.e("ProfileViewModel", "ID do usuário para carregar perfil é nulo.")

            _user.value = null

            _clubs.value = emptyList()

            _ratedBooks.value = emptyList()

            _isOwnProfile.value = false

            clubsListener?.remove()

            ratedBooksListener?.remove()

            return

        }



        _isOwnProfile.value = (idToFetch == currentUserId)



        clubsListener?.remove()

        ratedBooksListener?.remove()



        db.collection("users").document(idToFetch).get()

            .addOnSuccessListener { document ->

                if (document != null && document.exists()) {

                    val userObject = document.toObject(User::class.java)

                    _user.value = userObject

                    if (userObject != null) {

                        fetchUserClubs(userObject.uid, currentUserId)

                        fetchRatedBooks(userObject.uid)

                    } else {

                        Log.w("ProfileViewModel", "Falha ao converter documento do usuário. ID: $idToFetch")

                        _user.value = null; _clubs.value = emptyList(); _ratedBooks.value = emptyList()

                    }

                } else {

                    Log.w("ProfileViewModel", "Documento do usuário não encontrado. ID: $idToFetch")

                    _user.value = null; _clubs.value = emptyList(); _ratedBooks.value = emptyList()

                }

            }

            .addOnFailureListener { e ->

                Log.e("ProfileViewModel", "Falha ao buscar usuário $idToFetch", e)

                _user.value = null; _clubs.value = emptyList(); _ratedBooks.value = emptyList()

            }

    }



    private fun fetchRatedBooks(userId: String) {

        val query = db.collection("users").document(userId).collection("rated_books")

            .orderBy("ratedAt", Query.Direction.DESCENDING)



        ratedBooksListener = query.addSnapshotListener { snapshot, error ->

            if (error != null) {

                Log.w("ProfileViewModel", "Erro ao buscar livros avaliados para $userId.", error)

                _ratedBooks.value = emptyList()

                return@addSnapshotListener

            }

            _ratedBooks.value = snapshot?.toObjects(ProfileRatedBook::class.java) ?: emptyList()

            Log.d("ProfileViewModel", "Livros avaliados atualizados: ${_ratedBooks.value?.size ?: 0}")

        }

    }



    private fun fetchUserClubs(profileOwnerId: String, viewerId: String?) {

        clubsListener = db.collection("clubs").whereArrayContains("members", profileOwnerId)

            .addSnapshotListener { snapshot, error ->

                if (error != null) {

                    Log.w("ProfileViewModel", "Erro ao buscar clubes para $profileOwnerId.", error)

                    _clubs.value = emptyList()

                    return@addSnapshotListener

                }

                if (snapshot != null) {

                    val allClubs = snapshot.toObjects(BookClub::class.java)

                    _clubs.value = allClubs.filter { club ->

                        club.isPublic || club.members.contains(viewerId)

                    }

                } else {

                    _clubs.value = emptyList()

                }

            }

    }



    // --- Funções de Avaliação ---

    fun onBookSelectedForRating(book: BookItem) { _bookToRate.value = book }

    fun onRatingDialogDismiss() { _bookToRate.value = null }

    fun onRatingSubmitted(rating: Float) {

        val book = _bookToRate.value ?: return

        val uid = auth.currentUser?.uid ?: return

        // ... (criação do ratedBookData - sem mudanças) ...

        val ratedBookData = mapOf(

            "googleBookId" to book.id,

            "title" to book.volumeInfo?.title,

            "authors" to book.volumeInfo?.authors,

            "coverUrl" to book.volumeInfo?.imageLinks.getBestAvailableImageUrl(),

            "rating" to rating,

            "ratedAt" to FieldValue.serverTimestamp()

        )

        // ... (chamada ao Firestore add() - sem mudanças) ...

        db.collection("users").document(uid).collection("rated_books")

            .add(ratedBookData)

            .addOnSuccessListener { Log.d("ProfileVM", "Livro avaliado salvo: ${book.volumeInfo?.title}") }

            .addOnFailureListener { e -> Log.e("ProfileVM", "Erro ao salvar avaliação", e); _errorMessage.value = "Erro ao salvar avaliação." }

        _bookToRate.value = null

    }



    // --- Funções de Exclusão ---

    fun requestDeleteBook(book: ProfileRatedBook) { _bookToDelete.value = book }

    fun cancelDeleteBook() { _bookToDelete.value = null }

    fun confirmDeleteBook() {

        val book = _bookToDelete.value

        val userId = auth.currentUser?.uid

        if (book == null || userId == null || book.id.isBlank()) { /* ... (erro) ... */ return }



        viewModelScope.launch {

            try {

                db.collection("users").document(userId)

                    .collection("rated_books").document(book.id)

                    .delete()

                    .await()

                Log.d("ProfileVM", "Livro '${book.title}' excluído.")

                _toastMessage.value = "Livro removido da estante." // ✅ USA O TOAST AQUI

                _bookToDelete.value = null

            } catch (e: Exception) {

                Log.e("ProfileVM", "Erro ao excluir livro ${book.id}", e)

                _errorMessage.value = "Erro ao remover livro."

                _bookToDelete.value = null

            }

        }

    }



    // --- Funções de Atualização de Perfil ---

    fun updateUserProfile(name: String, aboutMe: String, newImageUri: Uri?) {
        viewModelScope.launch {
            _updateStatus.value = UpdateStatus.LOADING
            val uid = auth.currentUser?.uid ?: run { /* ... (tratamento de erro) ... */ return@launch }

            try {
                // Lógica de upload da imagem (se houver nova)
                val imageUrl = if (newImageUri != null) {
                    val imageRef = storage.reference.child("profile_pictures/$uid.jpg")
                    imageRef.putFile(newImageUri).await()
                    imageRef.downloadUrl.await().toString()
                } else {
                    user.value?.profilePictureUrl // Mantém a URL antiga se não houver nova imagem
                }

                // Atualiza o documento no Firestore
                updateUserDocument(uid, name, aboutMe, imageUrl)

            } catch (e: Exception) { /* ... (tratamento de erro) ... */ }
        }
    }

    private suspend fun updateUserDocument(uid: String, name: String, aboutMe: String, imageUrl: String?) {
        val userRef = db.collection("users").document(uid)
        val updates = mutableMapOf<String, Any>()
        updates["name"] = name
        updates["aboutMe"] = aboutMe
        imageUrl?.let { updates["profilePictureUrl"] = it } // Adiciona URL apenas se não for nula

        try {
            userRef.update(updates).await()
            _updateStatus.value = UpdateStatus.SUCCESS
            // Não precisa chamar loadUserProfile aqui, o listener do documento (se houvesse) atualizaria
            // Mas para garantir que a UI veja a mudança imediatamente após salvar (sem listener no doc user),
            // podemos forçar uma releitura ou atualizar o LiveData localmente.
            // A forma mais simples é recarregar, mas pode ser otimizado.
            loadUserProfile(uid) // Recarrega para mostrar dados atualizados
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "Erro ao atualizar documento do usuário $uid", e)
            _updateStatus.value = UpdateStatus.ERROR
            _errorMessage.value = "Falha ao salvar alterações."
        }
    }

    fun resetUpdateStatus() {
        _updateStatus.value = UpdateStatus.IDLE
        _errorMessage.value = null
    }


    // ⬇️ --- ADICIONADO: Função para resetar o Toast --- ⬇️

    /** Chamado pela UI depois que a mensagem Toast foi exibida */

    fun onToastMessageShown() {

        _toastMessage.value = null

    }

    // ⬆️ --- FIM DA ADIÇÃO --- ⬆️



    // Limpa listeners ao destruir ViewModel

    override fun onCleared() {

        super.onCleared()

        clubsListener?.remove()

        ratedBooksListener?.remove()

        Log.d("ProfileViewModel", "ViewModel cleared, listeners removidos.")

    }

}