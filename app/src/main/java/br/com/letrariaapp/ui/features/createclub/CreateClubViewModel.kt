package br.com.letrariaapp.ui.features.createclub

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.letrariaapp.data.BookClub
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

sealed class CreateClubResult {
    data object IDLE : CreateClubResult()
    data object LOADING : CreateClubResult()
    data object SUCCESS : CreateClubResult()
    data class ERROR(val message: String) : CreateClubResult()
}

class CreateClubViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private val _createClubResult = MutableLiveData<CreateClubResult>(CreateClubResult.IDLE)
    val createClubResult: LiveData<CreateClubResult> = _createClubResult

    fun createClub(name: String, description: String, isPublic: Boolean, maxMembers: Int) {
        if (name.isBlank() || description.isBlank()) {
            _createClubResult.value = CreateClubResult.ERROR("Nome e descrição são obrigatórios.")
            return
        }

        viewModelScope.launch {
            _createClubResult.value = CreateClubResult.LOADING

            val adminId = auth.currentUser?.uid
            if (adminId == null) {
                _createClubResult.value = CreateClubResult.ERROR("Usuário não autenticado.")
                return@launch
            }

            val clubId = db.collection("clubs").document().id
            val inviteCode = if (!isPublic) generateClubCode() else null

            // ✅ IMAGEM PADRÃO ALEATÓRIA
            val randomImageId = Random.nextInt(1, 1000) // Gera um número aleatório
            val defaultImageUrl = "https://picsum.photos/seed/$randomImageId/400/200"

            val newClub = BookClub(
                id = clubId,
                name = name,
                description = description,
                isPublic = isPublic,
                code = inviteCode,
                adminId = adminId,
                members = listOf(adminId),
                maxMembers = maxMembers,
                imageUrl = defaultImageUrl
            )

            try {
                db.collection("clubs").document(clubId).set(newClub).await()
                _createClubResult.value = CreateClubResult.SUCCESS
                Log.d("CreateClubViewModel", "Clube criado com sucesso: $clubId")
            } catch (e: Exception) {
                Log.e("CreateClubViewModel", "Erro ao criar clube", e)
                _createClubResult.value = CreateClubResult.ERROR(e.message ?: "Erro ao salvar clube.")
            }
        }
    }

    private fun generateClubCode(): String {
        val chars = ('A'..'Z') + ('0'..'9')
        return (1..6)
            .map { chars.random() }
            .joinToString("")
    }

    fun resetResult() {
        _createClubResult.value = CreateClubResult.IDLE
    }
}