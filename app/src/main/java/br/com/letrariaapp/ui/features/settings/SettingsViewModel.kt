package br.com.letrariaapp.ui.features.settings

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SettingsViewModel : ViewModel() {
    private val auth = Firebase.auth

    // Expõe diretamente o email do usuário logado. Retorna "Carregando..." se for nulo.
    val userEmail: String
        get() = auth.currentUser?.email ?: "Carregando..."
}