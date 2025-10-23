package br.com.letrariaapp

import android.content.pm.PackageManager
import br.com.letrariaapp.BuildConfig
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
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
// ✅ --- IMPORTS ADICIONADOS PARA UPDATE CHECK ---
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlinx.coroutines.tasks.await
import br.com.letrariaapp.data.BookClub
import br.com.letrariaapp.ui.features.home.HomeScreen
// ---
import br.com.letrariaapp.ui.features.auth.AuthViewModel
import br.com.letrariaapp.ui.features.auth.LoginScreen
import br.com.letrariaapp.ui.features.auth.RegisterScreen
// ... (resto dos seus imports normais)
import br.com.letrariaapp.ui.features.settings.TermsAndPoliciesScreen
import br.com.letrariaapp.ui.theme.LetrariaAppTheme
import com.google.firebase.auth.ktx.auth


// ✅ --- DATA CLASS PARA GUARDAR INFO DA ATUALIZAÇÃO ---
private data class UpdateInfo(
    val versionName: String,
    val apkUrl: String
)

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
    val context = LocalContext.current // ✅ Contexto para pegar versão e abrir URL

    // ✅ --- ESTADO PARA GUARDAR INFORMAÇÕES DA ATUALIZAÇÃO ---
    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }

    // ✅ --- LÓGICA DE VERIFICAÇÃO DE ATUALIZAÇÃO ---
    LaunchedEffect(Unit) { // Executa uma vez quando o AppNavigation inicia
        val remoteConfig = Firebase.remoteConfig
        // Configurações para buscar valores rapidamente em debug (opcional)
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 0 else 3600 // 1 hora em release
        }
        remoteConfig.setConfigSettingsAsync(configSettings)

        // Define valores padrão (caso a busca falhe) - devem ser os mesmos do Console
        remoteConfig.setDefaultsAsync(mapOf(
            "latest_version_code" to 1L, // Use L para Long
            "latest_version_name" to "1.0",
            "latest_apk_url" to ""
        )).await() // Espera os padrões serem definidos

        try {
            // 1. Busca os valores mais recentes do Firebase
            remoteConfig.fetchAndActivate().await()
            Log.d("AppUpdate", "Remote config fetched and activated.")

            // 2. Lê os valores do Remote Config
            val latestVersionCode = remoteConfig.getLong("latest_version_code")
            val latestVersionName = remoteConfig.getString("latest_version_name")
            val latestApkUrl = remoteConfig.getString("latest_apk_url")

            // 3. Pega o versionCode atual do app
            val currentVersionCode = try {
                val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    pInfo.longVersionCode
                } else {
                    @Suppress("DEPRECATION")
                    pInfo.versionCode.toLong()
                }
            } catch (e: PackageManager.NameNotFoundException) {
                Log.e("AppUpdate", "Failed to get current version code", e)
                1L // Valor padrão se falhar
            }

            Log.d("AppUpdate", "Current Version: $currentVersionCode, Latest Version: $latestVersionCode")

            // 4. Compara as versões e verifica se a URL existe
            if (latestVersionCode > currentVersionCode && latestApkUrl.isNotBlank()) {
                Log.d("AppUpdate", "Update available: $latestVersionName, URL: $latestApkUrl")
                // 5. Atualiza o estado para mostrar o diálogo
                updateInfo = UpdateInfo(latestVersionName, latestApkUrl)
            } else {
                Log.d("AppUpdate", "No update available or URL missing.")
                updateInfo = null // Garante que o diálogo não apareça
            }

        } catch (e: Exception) {
            Log.w("AppUpdate", "Error fetching remote config", e)
            updateInfo = null // Garante que o diálogo não apareça em caso de erro
        }
    }

    // ✅ --- MOSTRA O DIÁLOGO SE updateInfo NÃO FOR NULO ---
    updateInfo?.let { info ->
        UpdateAvailableDialog(
            versionName = info.versionName,
            onUpdateClick = {
                // Abre o link de download no navegador
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(info.apkUrl))
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Log.e("AppUpdate", "Failed to open URL", e)
                    Toast.makeText(context, "Não foi possível abrir o link de download.", Toast.LENGTH_SHORT).show()
                }
                updateInfo = null // Fecha o diálogo após clicar
            },
            onDismiss = {
                updateInfo = null // Fecha o diálogo
            }
        )
    }


    val currentUser = Firebase.auth.currentUser
    val startDestination = if (currentUser != null && currentUser.isEmailVerified) "home" else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        // ... (resto do seu NavHost - NENHUMA MUDANÇA AQUI)
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
                onClubClick = { club ->
                    navController.navigate("club/${club.id}")
                }
            )
        }

        // ... (todas as suas outras rotas) ...

        composable("terms_and_policies") {
            TermsAndPoliciesScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}


// ✅ --- COMPOSABLE PARA O DIÁLOGO DE ATUALIZAÇÃO ---
@Composable
private fun UpdateAvailableDialog(
    versionName: String,
    onUpdateClick: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Atualização Disponível") },
        text = { Text("Uma nova versão ($versionName) do Letraria está disponível. Deseja atualizar agora?") },
        confirmButton = {
            TextButton(onClick = onUpdateClick) {
                Text("Atualizar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Agora Não")
            }
        }
    )
}