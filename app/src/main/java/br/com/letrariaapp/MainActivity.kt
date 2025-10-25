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
import android.util.Log
import br.com.letrariaapp.data.BookItem
import br.com.letrariaapp.ui.features.home.HomeScreen
import br.com.letrariaapp.ui.features.auth.AuthViewModel
import br.com.letrariaapp.ui.features.auth.LoginScreen
import br.com.letrariaapp.ui.features.auth.RegisterScreen
import br.com.letrariaapp.ui.features.club.AdminScreen
import br.com.letrariaapp.ui.features.club.AdminViewModel
import br.com.letrariaapp.ui.features.club.ClubScreen
import br.com.letrariaapp.ui.features.club.ClubViewModel
import br.com.letrariaapp.ui.features.club.DeleteClubResult
import br.com.letrariaapp.ui.features.club.EditClubScreen
import br.com.letrariaapp.ui.features.club.EditClubViewModel
import br.com.letrariaapp.ui.features.club.LeaveClubResult
import br.com.letrariaapp.ui.features.createclub.CreateClubResult
import br.com.letrariaapp.ui.features.createclub.CreateClubScreen
import br.com.letrariaapp.ui.features.createclub.CreateClubViewModel
import br.com.letrariaapp.ui.features.profile.EditProfileScreen
import br.com.letrariaapp.ui.features.friends.FriendsScreen
import br.com.letrariaapp.ui.features.profile.ProfileScreen
import br.com.letrariaapp.ui.features.profile.ProfileViewModel
import br.com.letrariaapp.ui.features.profile.UpdateStatus
import br.com.letrariaapp.ui.features.search.ClubDetailsViewModel
import br.com.letrariaapp.ui.features.search.JoinClubResult
import br.com.letrariaapp.ui.features.search.SearchClubScreen
import br.com.letrariaapp.ui.features.search.SearchClubViewModel
import br.com.letrariaapp.ui.features.settings.SettingsScreen
import br.com.letrariaapp.ui.features.settings.SettingsViewModel
import br.com.letrariaapp.ui.features.booksearch.BookSearchScreen
import br.com.letrariaapp.ui.features.searchuser.SearchUserScreen
import br.com.letrariaapp.ui.features.settings.TermsAndPoliciesScreen
import br.com.letrariaapp.ui.theme.LetrariaAppTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.os.Build
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import android.content.Intent
import androidx.navigation.NavHostController
import androidx.navigation.navDeepLink


class MainActivity : ComponentActivity() {
    private lateinit var navController: NavHostController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        setContent {
            LetrariaAppTheme {
                navController = rememberNavController()
                AppNavigation(navController = navController)
            }
        }
    }

    // ✅ CORREÇÃO 1: 'intent' agora é 'Intent' (não-nulo)
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (::navController.isInitialized) {
            // ✅ CORREÇÃO 2: handleDeepLink aceita 'intent' (não-nulo)
            navController.handleDeepLink(intent)
        }
    }
}

// Chave pública para o resultado da busca
const val BOOK_SEARCH_RESULT_KEY = "selected_book"

@Composable
fun AppNavigation(navController: NavHostController) { // ✅ CORREÇÃO 3: Recebe o NavController
    // ❌ val navController = rememberNavController() // <- Esta linha foi removida
    val authViewModel: AuthViewModel = viewModel()

    val currentUser = Firebase.auth.currentUser
    val startDestination = if (currentUser != null && currentUser.isEmailVerified) "home" else "login"

    // Este bloco cuida de pedir a permissão de notificação no Android 13+
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                Log.d("NotificationPermission", "Permissão CONCEDIDA")
            } else {
                Log.w("NotificationPermission", "Permissão NEGADA")
            }
        }
    )

    LaunchedEffect(key1 = true) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Checa se a permissão AINDA NÃO foi dada
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                // Pede a permissão
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {

        // --- Rota Login ---
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

        // --- Rota Register ---
        composable("register") {
            RegisterScreen(
                viewModel = authViewModel,
                onLoginClick = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.popBackStack()
                }
            )
        }

        // --- Rota Home ---
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
                onClubClick = { club ->
                    navController.navigate("club/${club.id}")
                }
            )
        }

        // --- Rota Profile ---
        composable(
            route = "profile/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType; nullable = true }),
            deepLinks = listOf(
                navDeepLink { uriPattern = "letraria://profile/{userId}" }
            )
        ) { backStackEntry ->
            val profileViewModel: ProfileViewModel = viewModel()

            // --- CÓDIGO PARA RECEBER LIVRO (PROFILE) ---
            val bookResult = backStackEntry.savedStateHandle
                .getLiveData<BookItem>(BOOK_SEARCH_RESULT_KEY)
                .observeAsState()

            LaunchedEffect(bookResult.value) {
                bookResult.value?.let { bookItem ->
                    Log.d("ProfileNav", "Recebido o livro: ${bookItem.volumeInfo?.title}")
                    profileViewModel.onBookSelectedForRating(bookItem) // Chama ViewModel do Profile
                    backStackEntry.savedStateHandle.remove<BookItem>(BOOK_SEARCH_RESULT_KEY)
                }
            }
            // --- FIM CÓDIGO RECEBIMENTO (PROFILE) ---

            ProfileScreen(
                viewModel = profileViewModel,
                userId = backStackEntry.arguments?.getString("userId"),

                // --- Callbacks de Navegação ---
                onBackClick = { navController.popBackStack() },
                onEditProfileClick = { navController.navigate("edit_profile") },

                onLogoutClick = {
                    authViewModel.logout()
                    navController.navigate("login") { popUpTo(0) }
                },

                onSettingsClick = { navController.navigate("settings") },
                onClubClick = { club ->
                    navController.navigate("club/${club.id}")
                },
                onAddBookClick = { // Navega para busca de livro
                    navController.navigate("book_search")
                },
                onFriendsListClick = { //
                    navController.navigate("friends") // Navega para a tela de amigos
                }
            )
        }

        // --- Rota Edit Profile ---
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

            currentUserData?.let { user ->
                EditProfileScreen(
                    currentUser = user,
                    updateStatus = updateStatus,
                    onSaveClick = { name, aboutMe, newImageUri ->
                        profileViewModel.updateUserProfile(name, aboutMe, newImageUri)
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        // --- Rota Settings ---
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
                onDeleteAccount = { settingsViewModel.deleteAccount() },
                onTermsClick = { navController.navigate("terms_and_policies") }
            )
        }

        // --- Rota Create Club ---
        composable("create_club") {
            val viewModel: CreateClubViewModel = viewModel()
            val result by viewModel.createClubResult.observeAsState()
            val context = LocalContext.current

            LaunchedEffect(result) {
                when (val res = result) {
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

        // --- Rota Search Club ---
        composable("search_club") {
            val searchViewModel: SearchClubViewModel = viewModel()
            val detailsViewModel: ClubDetailsViewModel = viewModel()
            val joinResult by detailsViewModel.joinResult.observeAsState()
            val context = LocalContext.current

            LaunchedEffect(joinResult) {
                when (val result = joinResult) {
                    is JoinClubResult.SUCCESS -> {
                        Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                        detailsViewModel.resetJoinResult()
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true } // Se 'home' já estiver no stack
                            popUpTo("search_club") { inclusive = true } // Remove a tela de busca
                        }

                    }
                    is JoinClubResult.ERROR -> {
                        Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                        detailsViewModel.resetJoinResult()
                    }
                    else -> {}
                }
            }

            SearchClubScreen(
                viewModel = searchViewModel,
                onBackClick = { navController.popBackStack() },
                detailsViewModel = detailsViewModel,
                onClubClick = { club ->
                    navController.navigate("club/${club.id}")
                }
            )
        }

        // --- Rota Club ---
        composable(
            route = "club/{clubId}",
            arguments = listOf(navArgument("clubId") { type = NavType.StringType }),
            deepLinks = listOf(
                navDeepLink { uriPattern = "letraria://club/{clubId}" }
            )
        ) { backStackEntry ->
            val clubId = backStackEntry.arguments?.getString("clubId")
                ?: return@composable // Garante que clubId não seja nulo

            val clubViewModel: ClubViewModel = viewModel(key = clubId)

            val toastMessage by clubViewModel.toastMessage.observeAsState()
            val context = LocalContext.current

            LaunchedEffect(toastMessage) {
                toastMessage?.let { message ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    clubViewModel.onToastMessageShown()
                }
            }

            // --- CÓDIGO PARA RECEBER LIVRO (CLUB) ---
            val bookResult = backStackEntry.savedStateHandle
                .getLiveData<BookItem>(BOOK_SEARCH_RESULT_KEY)
                .observeAsState()

            LaunchedEffect(bookResult.value) {
                bookResult.value?.let { bookItem ->
                    Log.d("ClubNav", "Recebido o livro para indicar: ${bookItem.volumeInfo?.title}")
                    clubViewModel.onBookSelectedFromSearch(bookItem) // ⬅️ CHAMADA AQUI
                    backStackEntry.savedStateHandle.remove<BookItem>(BOOK_SEARCH_RESULT_KEY)
                }
            }
            // --- FIM CÓDIGO RECEBIMENTO (CLUB) ---

            ClubScreen(
                clubId = clubId,
                viewModel = clubViewModel,
                onBackClick = { navController.popBackStack() },
                onProfileClick = { userId ->
                    navController.navigate("profile/$userId")
                },
                onAdminClick = {
                    navController.navigate("club_admin/$clubId")
                },
                onSearchBookClick = {
                    navController.navigate("book_search")
                }
            )
        }

        // --- Rota Club Admin ---
        composable(
            route = "club_admin/{clubId}",
            arguments = listOf(navArgument("clubId") { type = NavType.StringType }),
            deepLinks = listOf(
                navDeepLink { uriPattern = "letraria://club_admin/{clubId}" }
            )
        ) { backStackEntry ->
            val clubId = backStackEntry.arguments?.getString("clubId")
            val adminViewModel: AdminViewModel = viewModel()
            val context = LocalContext.current

            val toastMessage by adminViewModel.toastMessage.observeAsState()
            LaunchedEffect(toastMessage) {
                toastMessage?.let { message ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    adminViewModel.onToastMessageShown()
                }
            }

            val leaveResult by adminViewModel.leaveResult.observeAsState(LeaveClubResult.IDLE)
            LaunchedEffect(leaveResult) {
                if (leaveResult is LeaveClubResult.SUCCESS) {
                    adminViewModel.resetLeaveResult()
                    navController.navigate("home") {
                        popUpTo(0)
                    }
                }
            }

            val deleteResult by adminViewModel.clubDeleted.observeAsState(DeleteClubResult.IDLE)
            LaunchedEffect(deleteResult) {
                if (deleteResult is DeleteClubResult.SUCCESS) {
                    navController.navigate("home") {
                        popUpTo(0)
                    }
                }
            }

            if (clubId != null) {
                AdminScreen(
                    clubId = clubId,
                    viewModel = adminViewModel,
                    onBackClick = { navController.popBackStack() },
                    onLeaveClub = { adminViewModel.leaveClub() },
                    onDrawUser = { adminViewModel.drawUserForCycle() },
                    onEditClub = { navController.navigate("edit_club/$clubId") },
                    onProfileClick = { userId -> navController.navigate("profile/$userId") }
                )
            }
        }

        // --- Rota Edit Club ---
        composable(
            route = "edit_club/{clubId}",
            arguments = listOf(navArgument("clubId") { type = NavType.StringType })
        ) { backStackEntry ->
            val clubId = backStackEntry.arguments?.getString("clubId")
            val editViewModel: EditClubViewModel = viewModel()
            if (clubId != null) {
                EditClubScreen(
                    clubId = clubId,
                    viewModel = editViewModel,
                    onBackClick = { navController.popBackStack() },
                    onSaveSuccess = { navController.popBackStack() }
                )
            }
        }

        // --- Rota Terms and Policies ---
        composable("terms_and_policies") {
            TermsAndPoliciesScreen(
                onBackClick = { navController.popBackStack() }
            )
        }


        // --- Rota Book Search ---
        composable("book_search") {
            BookSearchScreen(
                onBookSelected = { selectedBook ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(BOOK_SEARCH_RESULT_KEY, selectedBook)

                    Log.d("BookSearch", "Livro selecionado: ${selectedBook.volumeInfo?.title}")

                    navController.popBackStack()
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // --- Rota Search User ---
        composable("search_user") {
            SearchUserScreen(
                onUserClick = { userId ->
                    navController.navigate("profile/$userId")
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // --- Rota Friends ---
        composable(
            route = "friends?tab={tabIndex}", // Rota modificada
            arguments = listOf( // Argumento adicionado
                navArgument("tabIndex") {
                    defaultValue = "0" // Aba "Amigos" é o padrão
                    type = NavType.StringType
                }
            ),
            deepLinks = listOf( // Deep Link adicionado
                navDeepLink { uriPattern = "letraria://friends?tab={tabIndex}" }
            )
        ) { backStackEntry ->
            FriendsScreen(
                viewModel = viewModel(),
                // ✅ CORREÇÃO 4: O erro 'startTabIndex' está aqui.
                // Esta chamada para FriendsScreen precisa ser atualizada.
                // Vamos consertar isso no PRÓXIMO passo, quando você me mandar
                // o arquivo FriendsScreen.kt.
                startTabIndex = backStackEntry.arguments?.getString("tabIndex")?.toIntOrNull() ?: 0,
                onBackClick = { navController.popBackStack() },
                onSearchUserClick = {
                    navController.navigate("search_user")
                },
                onProfileClick = { userId ->
                    navController.navigate("profile/$userId")
                }
            )
        }

    }
}