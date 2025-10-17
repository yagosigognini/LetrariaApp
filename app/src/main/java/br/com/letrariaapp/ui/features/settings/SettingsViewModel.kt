package br.com.letrariaapp.ui.features.settings

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SettingsViewModel : ViewModel() {
    private val auth = Firebase.auth

    private val _toastMessage = MutableLiveData<String?>(null)
    val toastMessage: LiveData<String?> = _toastMessage

    // NOVO: LiveData para sinalizar que a conta foi excluída
    private val _accountDeleted = MutableLiveData<Boolean>(false)
    val accountDeleted: LiveData<Boolean> = _accountDeleted

    val userEmail: String
        get() = auth.currentUser?.email ?: "Carregando..."

    fun sendPasswordResetEmail() {
        // ... (código existente)
    }

    // NOVO: Função para deletar a conta do usuário
    fun deleteAccount() {
        viewModelScope.launch {
            val user = auth.currentUser
            if (user == null) {
                _toastMessage.value = "Nenhum usuário para excluir."
                return@launch
            }

            try {
                user.delete().await()
                Log.d("SettingsViewModel", "Conta do usuário excluída com sucesso.")
                _accountDeleted.value = true // Sinaliza para a UI que a exclusão foi bem-sucedida
            } catch (e: Exception) {
                Log.w("SettingsViewModel", "Erro ao excluir conta:", e)
                if (e is FirebaseAuthRecentLoginRequiredException) {
                    _toastMessage.value = "Por segurança, faça o login novamente antes de excluir a conta."
                } else {
                    _toastMessage.value = "Erro ao excluir a conta. Tente novamente."
                }
            }
        }
    }

    fun onToastMessageShown() {
        _toastMessage.value = null
    }
}