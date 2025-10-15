// Local: br/com/letrariaapp/MainActivity.kt

package br.com.letrariaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.com.letrariaapp.ui.features.auth.LoginScreen
import br.com.letrariaapp.ui.features.auth.RegisterScreen
import br.com.letrariaapp.ui.theme.LetrariaAppTheme
import br.com.letrariaapp.ui.features.home.HomeScreen
import br.com.letrariaapp.ui.features.profile.ProfileScreen // <-- ADICIONE O IMPORT
import br.com.letrariaapp.ui.features.profile.sampleUser // <-- ADICIONE O IMPORT DOS DADOS DE EXEMPLO
import br.com.letrariaapp.ui.features.home.sampleUserForHome

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // O nome do seu tema aqui, conforme definido em ui/theme/Theme.kt
            LetrariaAppTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            LoginScreen(
                onRegisterClick = { navController.navigate("register") },
                onLoginClick = { email, password ->
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("register") {
            RegisterScreen(
                onLoginClick = { navController.popBackStack() },
                onRegisterClick = { nome, email, password ->
                    navController.popBackStack()
                }
            )
        }

        composable("home") {
            HomeScreen(
                // MUDANÇA 1: Adicionando a ação de navegação
                user = sampleUserForHome, // <-- PASSE O USUÁRIO DE EXEMPLO
                onProfileClick = { navController.navigate("profile") },
                onSettingsClick = { /* Navegar para configurações */ },
                onCreateClubClick = { /* Navegar para criar clube */ },
                onJoinClubClick = { /* Navegar para procurar clubes */ },
                onClubClick = { club ->
                    println("Clicou no clube: ${club.name}")
                }
            )
        }

        // MUDANÇA 2: Adicionando a nova rota para a tela de perfil
        composable("profile") {
            ProfileScreen(
                user = sampleUser, // Usando dados de exemplo por enquanto
                onBackClick = {
                    // Ação para voltar para a tela anterior
                    navController.popBackStack()
                },
                onSettingsClick = { /* TODO: Abrir menu de configurações */ }
            )
        }
    }
}