package br.com.letrariaapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.com.letrariaapp.ui.features.auth.AuthViewModel
import br.com.letrariaapp.ui.features.auth.LoginScreen
import br.com.letrariaapp.ui.features.auth.RegisterScreen
import br.com.letrariaapp.ui.features.createclub.CreateClubScreen
import br.com.letrariaapp.ui.features.home.HomeScreen
import br.com.letrariaapp.ui.features.profile.EditProfileScreen
import br.com.letrariaapp.ui.features.profile.ProfileScreen
import br.com.letrariaapp.ui.features.profile.ProfileViewModel
import br.com.letrariaapp.ui.features.profile.UpdateStatus
import br.com.letrariaapp.ui.features.settings.SettingsScreen
import br.com.letrariaapp.ui.theme.LetrariaAppTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        setContent {
            LetrariaAppTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()

    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            LoginScreen(
                viewModel = authViewModel,
                onRegisterClick = { navController.navigate("register") },
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("register") {
            RegisterScreen(
                viewModel = authViewModel,
                onLoginClick = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.popBackStack()
                }
            )
        }

        composable("home") {
            // ✅ CORREÇÃO APLICADA:
            // A chamada para HomeScreen não passa mais o 'user'.
            // Ela agora usa o HomeViewModel internamente para buscar os dados.
            HomeScreen(
                onProfileClick = {
                    val userId = Firebase.auth.currentUser?.uid
                    if (userId != null) {
                        navController.navigate("profile/$userId")
                    }
                },
                // ✅ MUDANÇA: O botão de engrenagem agora navega para a tela de configurações
                onSettingsClick = { navController.navigate("settings") },
                onCreateClubClick = { navController.navigate("create_club") },
                onJoinClubClick = { /* TODO */ },
                onClubClick = { /* TODO */ }
            )
        }

        composable(
            route = "profile/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType; nullable = true })
        ) { backStackEntry ->
            ProfileScreen(
                userId = backStackEntry.arguments?.getString("userId"),
                onBackClick = { navController.popBackStack() },
                onEditProfileClick = { navController.navigate("edit_profile") },
                onLogoutClick = {
                    authViewModel.logout()
                    navController.navigate("login") { popUpTo(0) }
                }
            )
        }

        composable("edit_profile") {
            val profileViewModel: ProfileViewModel = viewModel(navController.previousBackStackEntry!!)
            val currentUser by profileViewModel.user.observeAsState()
            val updateStatus by profileViewModel.updateStatus.observeAsState(UpdateStatus.IDLE)
            val errorMessage by profileViewModel.errorMessage.observeAsState()
            val context = LocalContext.current

            LaunchedEffect(updateStatus) {
                if (updateStatus == UpdateStatus.SUCCESS) {
                    Toast.makeText(context, "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                    profileViewModel.resetUpdateStatus()
                }
            }

            LaunchedEffect(errorMessage) {
                errorMessage?.let { error ->
                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                    profileViewModel.resetUpdateStatus()
                }
            }

            if (currentUser != null) {
                EditProfileScreen(
                    currentUser = currentUser!!,
                    updateStatus = updateStatus,
                    onSaveClick = { name, aboutMe, newImageUri ->
                        profileViewModel.updateUserProfile(name, aboutMe, newImageUri)
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        // ✅ NOVA ROTA: Adicionada a rota para a tela de Configurações
        composable("settings") {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onLogoutClick = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo(0)
                    }
                }
            )
        }

        composable("create_club") {
            CreateClubScreen(
                onClose = { navController.popBackStack() }
            )
        }
    }
}