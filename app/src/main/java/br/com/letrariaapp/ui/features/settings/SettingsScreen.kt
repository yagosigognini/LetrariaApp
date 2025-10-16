package br.com.letrariaapp.ui.features.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.letrariaapp.R // Import para R.drawable
import br.com.letrariaapp.ui.components.AppBackground
import br.com.letrariaapp.ui.theme.LetrariaAppTheme

// --- TELA "INTELIGENTE" (STATEFUL) ---
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val userEmail by remember(viewModel.userEmail) {
        mutableStateOf(viewModel.userEmail)
    }

    SettingsScreenContent(
        userEmail = userEmail,
        onBackClick = onBackClick,
        onPasswordResetClick = { /* TODO */ },
        onThemeChangeClick = { /* TODO */ },
        onFeedbackClick = { /* TODO */ },
        onTermsClick = { /* TODO */ },
        onLogoutClick = onLogoutClick,
        onDeleteAccountClick = { /* TODO */ }
    )
}

// --- TELA "BURRA" (STATELESS) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenContent(
    userEmail: String,
    onBackClick: () -> Unit,
    onPasswordResetClick: () -> Unit,
    onThemeChangeClick: () -> Unit,
    onFeedbackClick: () -> Unit,
    onTermsClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onDeleteAccountClick: () -> Unit
) {
    AppBackground(backgroundResId = R.drawable.background) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Configurações") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Seção de Conta
                Text("Conta", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                InfoRow(label = "Email", value = userEmail)
                ClickableRow(label = "Senha", value = "Redefinir", onClick = onPasswordResetClick)

                Spacer(modifier = Modifier.height(24.dp))

                // Seção de Preferências
                Text("Preferências", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                ClickableRow(label = "Tema", value = "Claro", onClick = onThemeChangeClick)

                Spacer(modifier = Modifier.height(24.dp))

                // Seção "Sobre"
                Text("Sobre", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                ClickableRow(label = "Enviar Feedback", onClick = onFeedbackClick)
                ClickableRow(label = "Termos e Políticas", onClick = onTermsClick)

                Spacer(modifier = Modifier.height(32.dp))

                // Ações Finais
                Button(
                    onClick = onLogoutClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sair da conta", color = MaterialTheme.colorScheme.onSecondaryContainer)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDeleteAccountClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Excluir conta", color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }
    }
}


// --- COMPONENTES AUXILIARES ---

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text(value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    HorizontalDivider()
}

@Composable
private fun ClickableRow(label: String, value: String? = null, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (value != null) {
                Text(value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    HorizontalDivider()
}

// --- PREVIEW ---

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    LetrariaAppTheme {
        SettingsScreenContent(
            userEmail = "fulanodetal@gmail.com",
            onBackClick = {},
            onPasswordResetClick = {},
            onThemeChangeClick = {},
            onFeedbackClick = {},
            onTermsClick = {},
            onLogoutClick = {},
            onDeleteAccountClick = {}
        )
    }
}