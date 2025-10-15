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
            LoginScreen( // Supondo que você tenha a LoginScreen
                onRegisterClick = { navController.navigate("register") },
                onLoginClick = { email, password ->
                    // Navega para a Home e limpa a tela de login do histórico
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("register") {
            RegisterScreen( // Supondo que você tenha a RegisterScreen
                onLoginClick = { navController.popBackStack() },
                onRegisterClick = { nome, email, password ->
                    navController.popBackStack()
                }
            )
        }

        composable("home") {
            HomeScreen(
                onProfileClick = { /* Navegar para perfil */ },
                onSettingsClick = { /* Navegar para configurações */ },
                onCreateClubClick = { /* Navegar para criar clube */ },
                onJoinClubClick = { /* Navegar para procurar clubes */ },
                onClubClick = { club ->
                    println("Clicou no clube: ${club.name}")
                }
            )
        }
    }
}