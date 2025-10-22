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
import br.com.letrariaapp.ui.features.club.ClubScreen
import br.com.letrariaapp.ui.features.club.ClubViewModel
import br.com.letrariaapp.ui.features.club.AdminScreen
import br.com.letrariaapp.ui.features.createclub.CreateClubResult
import br.com.letrariaapp.ui.features.createclub.CreateClubScreen
import br.com.letrariaapp.ui.features.createclub.CreateClubViewModel
import br.com.letrariaapp.ui.features.home.HomeScreen
import br.com.letrariaapp.ui.features.profile.EditProfileScreen
import br.com.letrariaapp.ui.features.profile.ProfileScreen
import br.com.letrariaapp.ui.features.profile.ProfileViewModel
import br.com.letrariaapp.ui.features.profile.UpdateStatus
import br.com.letrariaapp.ui.features.search.ClubDetailsViewModel
import br.com.letrariaapp.ui.features.search.JoinClubResult
import br.com.letrariaapp.ui.features.search.SearchClubScreen
import br.com.letrariaapp.ui.features.settings.SettingsScreen
import br.com.letrariaapp.ui.features.settings.SettingsViewModel
import br.com.letrariaapp.ui.theme.LetrariaAppTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase



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

    val currentUser = Firebase.auth.currentUser
    val startDestination = if (currentUser != null && currentUser.isEmailVerified) {
        "home"
    } else {
        "login"
    }

    NavHost(navController = navController, startDestination = startDestination) {

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
            HomeScreen(
                onProfileClick = {
                    val userId = Firebase.auth.currentUser?.uid
                    if (userId != null) {
                        navController.navigate("profile/$userId")
                    }
                },
                onSettingsClick = { navController.navigate("settings") },
                onCreateClubClick = { navController.navigate("create_club") },
                onJoinClubClick = { navController.navigate("search_club") },
                // ✅ CONEXÃO FEITA AQUI
                onClubClick = { club ->
                    navController.navigate("club/${club.id}")
                }
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
                },
                onSettingsClick = { navController.navigate("settings") },
                onClubClick = { club ->
                    navController.navigate("club/${club.id}")
                }
            )
        }

        composable("edit_profile") {
            val profileViewModel: ProfileViewModel = viewModel(navController.previousBackStackEntry!!)
            val currentUserData by profileViewModel.user.observeAsState()
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

            if (currentUserData != null) {
                EditProfileScreen(
                    currentUser = currentUserData!!,
                    updateStatus = updateStatus,
                    onSaveClick = { name, aboutMe, newImageUri ->
                        profileViewModel.updateUserProfile(name, aboutMe, newImageUri)
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        composable("settings") {
            val settingsViewModel: SettingsViewModel = viewModel()
            val toastMessage by settingsViewModel.toastMessage.observeAsState()
            val accountDeleted by settingsViewModel.accountDeleted.observeAsState(false)
            val context = LocalContext.current

            LaunchedEffect(toastMessage) {
                toastMessage?.let { message ->
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    settingsViewModel.onToastMessageShown()
                }
            }

            LaunchedEffect(accountDeleted) {
                if (accountDeleted) {
                    navController.navigate("login") {
                        popUpTo(0)
                    }
                }
            }

            SettingsScreen(
                viewModel = settingsViewModel,
                onBackClick = { navController.popBackStack() },
                onLogoutClick = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo(0)
                    }
                },
                onDeleteAccount = { settingsViewModel.deleteAccount() }
            )
        }

        composable("create_club") {
            val viewModel: CreateClubViewModel = viewModel()
            val result by viewModel.createClubResult.observeAsState()
            val context = LocalContext.current

            LaunchedEffect(result) {
                when(val res = result) {
                    is CreateClubResult.SUCCESS -> {
                        Toast.makeText(context, "Clube criado com sucesso!", Toast.LENGTH_SHORT).show()
                        viewModel.resetResult()
                        navController.popBackStack()
                    }
                    is CreateClubResult.ERROR -> {
                        Toast.makeText(context, res.message, Toast.LENGTH_LONG).show()
                        viewModel.resetResult()
                    }
                    else -> {}
                }
            }

            CreateClubScreen(
                viewModel = viewModel,
                onClose = { navController.popBackStack() }
            )
        }

        composable("search_club") {
            val detailsViewModel: ClubDetailsViewModel = viewModel()
            val joinResult by detailsViewModel.joinResult.observeAsState()
            val context = LocalContext.current

            LaunchedEffect(joinResult) {
                when (val result = joinResult) {
                    is JoinClubResult.SUCCESS -> {
                        Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                        detailsViewModel.resetJoinResult()
                    }
                    is JoinClubResult.ERROR -> {
                        Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                        detailsViewModel.resetJoinResult()
                    }
                    else -> {}
                }
            }

            SearchClubScreen(
                onBackClick = { navController.popBackStack() },
                detailsViewModel = detailsViewModel,
                onClubClick = { club ->
                    navController.navigate("club/${club.id}")
                }
            )
        }

        composable(
            route = "club/{clubId}",
            arguments = listOf(navArgument("clubId") { type = NavType.StringType })
        ) { backStackEntry ->
            val clubId = backStackEntry.arguments?.getString("clubId")
            val clubViewModel: ClubViewModel = viewModel()
            val toastMessage by clubViewModel.toastMessage.observeAsState()
            val context = LocalContext.current

            LaunchedEffect(toastMessage) {
                toastMessage?.let { message ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    clubViewModel.onToastMessageShown()
                }
            }

            if (clubId != null) {
                ClubScreen(
                    clubId = clubId,
                    viewModel = clubViewModel,
                    onBackClick = { navController.popBackStack() },
                    onProfileClick = { userId ->
                        navController.navigate("profile/$userId")
                    },
                    // ✅ Ação para o novo botão de admin
                    onAdminClick = {
                        navController.navigate("club_admin/$clubId")
                    }
                )
            }
        }

        // ✅ NOVA ROTA PARA A TELA ADMIN
        composable(
            route = "club_admin/{clubId}",
            arguments = listOf(navArgument("clubId") { type = NavType.StringType })
        ) { backStackEntry ->
            val clubId = backStackEntry.arguments?.getString("clubId")
            if (clubId != null) {
                AdminScreen(
                    clubId = clubId,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}