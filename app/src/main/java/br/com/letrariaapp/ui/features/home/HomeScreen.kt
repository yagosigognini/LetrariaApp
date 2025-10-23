package br.com.letrariaapp.ui.features.home

// ... Imports normais
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
// ... (Imports de Lógica de Atualização - MANTENHA-OS AQUI)
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import br.com.letrariaapp.BuildConfig
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlinx.coroutines.tasks.await
// ---
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.letrariaapp.R
import br.com.letrariaapp.data.BookClub
import br.com.letrariaapp.data.Quote
import br.com.letrariaapp.data.User
import br.com.letrariaapp.data.sampleClubsList
import br.com.letrariaapp.data.sampleQuote
import br.com.letrariaapp.data.sampleUser
import br.com.letrariaapp.ui.components.AppBackground // ✅ Verifique se este import está correto
import br.com.letrariaapp.ui.components.ClubsSection
import br.com.letrariaapp.ui.theme.LetrariaAppTheme
import coil.compose.AsyncImage

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.systemBars

// Data class UpdateInfo (MANTENHA AQUI)
private data class UpdateInfo(
    val versionName: String,
    val apkUrl: String
)

val homeButtonColor = Color(0xFFE57373)
val homeTextColor = Color(0xFF2A3A6A)

// --- TELA "INTELIGENTE" (STATEFUL) ---
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onCreateClubClick: () -> Unit,
    onJoinClubClick: () -> Unit,
    onClubClick: (BookClub) -> Unit
) {
    val user by viewModel.user.observeAsState()
    val clubs by viewModel.clubs.observeAsState(emptyList())
    val context = LocalContext.current

    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }

    // Lógica de Verificação de Atualização (MANTENHA AQUI)
    LaunchedEffect(Unit) {
        val remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 0 else 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        // ... (resto da lógica de fetch e activate) ...
        remoteConfig.setDefaultsAsync(mapOf(
            "latest_version_code" to 1L,
            "latest_version_name" to "1.0",
            "latest_apk_url" to ""
        )).await()

        try {
            remoteConfig.fetchAndActivate().await()
            Log.d("AppUpdate", "Remote config fetched and activated.")

            val latestVersionCode = remoteConfig.getLong("latest_version_code")
            val latestVersionName = remoteConfig.getString("latest_version_name")
            val latestApkUrl = remoteConfig.getString("latest_apk_url")

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
                1L
            }

            Log.d("AppUpdate", "Current Version: $currentVersionCode, Latest Version: $latestVersionCode")

            if (latestVersionCode > currentVersionCode && latestApkUrl.isNotBlank()) {
                Log.d("AppUpdate", "Update available: $latestVersionName, URL: $latestApkUrl")
                updateInfo = UpdateInfo(latestVersionName, latestApkUrl)
            } else {
                Log.d("AppUpdate", "No update available or URL missing.")
                updateInfo = null
            }

        } catch (e: Exception) {
            Log.w("AppUpdate", "Error fetching remote config", e)
            updateInfo = null
        }
    }

    // Mostra o Diálogo (MANTENHA AQUI)
    updateInfo?.let { info ->
        UpdateAvailableDialog(
            versionName = info.versionName,
            onUpdateClick = { /* ... (lógica para abrir URL) ... */
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(info.apkUrl))
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Log.e("AppUpdate", "Failed to open URL", e)
                    Toast.makeText(context, "Não foi possível abrir o link de download.", Toast.LENGTH_SHORT).show()
                }
                updateInfo = null
            },
            onDismiss = {
                updateInfo = null
            }
        )
    }

    HomeScreenContent(
        user = user,
        quote = sampleQuote,
        clubs = clubs,
        onProfileClick = onProfileClick,
        onSettingsClick = onSettingsClick,
        onCreateClubClick = onCreateClubClick,
        onJoinClubClick = onJoinClubClick,
        onClubClick = onClubClick
    )
}

// --- TELA "BURRA" (STATELESS) ---
@Composable
fun HomeScreenContent(
    user: User?,
    quote: Quote,
    clubs: List<BookClub>,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onCreateClubClick: () -> Unit,
    onJoinClubClick: () -> Unit,
    onClubClick: (BookClub) -> Unit
) {
    // ✅ --- CORREÇÃO AQUI ---
    // Voltando a usar backgroundResId com a imagem correta para esta tela
    AppBackground(backgroundResId = R.drawable.app_background) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = WindowInsets.systemBars.asPaddingValues()
        ) {
            item {
                HomeTopBar(
                    profileImageUrl = user?.profilePictureUrl,
                    onProfileClick = onProfileClick,
                    onSettingsClick = onSettingsClick
                )
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
            item { QuoteSection(quote = quote) }
            item { Spacer(modifier = Modifier.height(32.dp)) }

            if (clubs.isEmpty()) {
                item {
                    EmptyClubsSection()
                }
            } else {
                item {
                    ClubsSection(
                        title = "Meus Clubes",
                        clubs = clubs,
                        onClubClick = onClubClick
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
            item { ActionButtons(onCreateClubClick, onJoinClubClick) }


        }
    }
}

// --- COMPONENTES ---
// ... (HomeTopBar, QuoteSection, ActionButtons, EmptyClubsSection - Sem Mudanças) ...
@Composable
fun HomeTopBar(
    profileImageUrl: String?,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = profileImageUrl?.ifEmpty { R.drawable.ic_launcher_background },
            contentDescription = "Foto de Perfil",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .clickable(onClick = onProfileClick),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.ic_launcher_background),
            error = painterResource(id = R.drawable.ic_launcher_background)
        )

        IconButton(onClick = onSettingsClick) {
            Icon(Icons.Default.Settings, contentDescription = "Configurações", tint = homeTextColor)
        }
    }
}

@Composable
fun QuoteSection(quote: Quote) {
    Column(
        modifier = Modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = quote.text,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = homeTextColor
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = quote.source,
            style = MaterialTheme.typography.bodySmall,
            color = homeTextColor.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun ActionButtons(onCreateClubClick: () -> Unit, onJoinClubClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = onCreateClubClick,
            modifier = Modifier
                .weight(1f)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = homeButtonColor)
        ) {
            Text("CRIAR CLUBE", color = Color.White)
        }
        Button(
            onClick = onJoinClubClick,
            modifier = Modifier
                .weight(1f)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = homeButtonColor)
        ) {
            Text("BUSCAR CLUBES", color = Color.White)
        }
    }
}

@Composable
fun EmptyClubsSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Você ainda não está em nenhum clube",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = homeTextColor
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Crie um novo clube ou use a busca para encontrar um clube e participar!",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = homeTextColor.copy(alpha = 0.8f)
        )
    }
}

// --- PREVIEWS ---
// ... (Previews - Sem Mudanças) ...
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    LetrariaAppTheme {
        HomeScreenContent(
            user = sampleUser,
            quote = sampleQuote,
            clubs = sampleClubsList,
            onProfileClick = {},
            onSettingsClick = {},
            onCreateClubClick = {},
            onJoinClubClick = {}
        ) {}
        // ⚠️ TESTE TEMPORÁRIO

    }
}

@Preview(showBackground = true, name = "Home Screen Vazia")
@Composable
fun HomeScreenEmptyPreview() {
    LetrariaAppTheme {
        HomeScreenContent(
            user = sampleUser,
            quote = sampleQuote,
            clubs = emptyList(),
            onProfileClick = {},
            onSettingsClick = {},
            onCreateClubClick = {},
            onJoinClubClick = {},
            onClubClick = {}
        )
    }
}


// Diálogo de Atualização (MANTENHA AQUI)
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