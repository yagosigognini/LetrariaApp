package br.com.letrariaapp.ui.features.createclub

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import br.com.letrariaapp.R
import br.com.letrariaapp.ui.components.AppBackground
import br.com.letrariaapp.ui.features.home.homeButtonColor
import br.com.letrariaapp.ui.features.home.homeTextColor
import br.com.letrariaapp.ui.theme.LetrariaAppTheme
import coil.compose.AsyncImage

enum class ClubMode { PRIVATE, PUBLIC }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateClubScreen(
    onClose: () -> Unit
) {
    // --- NOVOS ESTADOS ---
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // --- NOVO LAUNCHER ---
    // Este é o "lançador" que abre a galeria e nos devolve a imagem escolhida
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        // Quando o usuário escolhe uma imagem, a URI dela é salva no nosso estado
        imageUri = uri
    }

    var clubName by remember { mutableStateOf("") }
    var clubMode by remember { mutableStateOf(ClubMode.PRIVATE) }
    var maxMembers by remember { mutableStateOf(10) }
    var description by remember { mutableStateOf("") }
    var showHelpDialog by remember { mutableStateOf(false) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    val memberOptions = (2..20).toList() // Começando de 2, pois um clube tem pelo menos o criador
    val maxDescriptionChars = 140

    AppBackground(backgroundResId = R.drawable.background) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    // --- LÓGICA DA IMAGEM ATUALIZADA ---
                    // Se o usuário escolheu uma imagem (imageUri != null), mostre-a.
                    // Senão, mostre uma imagem padrão.
                    AsyncImage(
                        model = imageUri ?: R.drawable.ic_launcher_background, // Usa a URI ou um drawable padrão
                        contentDescription = "Capa do Clube",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                    IconButton(onClick = onClose, modifier = Modifier.align(Alignment.TopEnd)) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White)
                    }
                    TextButton(
                        // --- AÇÃO DO BOTÃO ATUALIZADA ---
                        // Ao clicar, "lança" o seletor de imagens
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
                    ) {
                        Text("Alterar Capa", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                Column(modifier = Modifier.padding(24.dp)) {
                    OutlinedTextField(
                        value = clubName,
                        onValueChange = { clubName = it },
                        label = { Text("Nome") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Column {
                        // Texto "Modo" em sua própria linha
                        Text("Modo", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(8.dp)) // Espaço entre o texto e o seletor

                        // Usamos um Row apenas para o seletor e o ícone de ajuda
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ModeSelector(
                                selectedMode = clubMode,
                                onModeSelected = { clubMode = it }
                            )
                            IconButton(onClick = { showHelpDialog = true }) {
                                Icon(Icons.Default.Info, contentDescription = "Ajuda sobre os modos", tint = homeTextColor)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // --- LAYOUT DO SELETOR DE MEMBROS ATUALIZADO ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Número máximo de membros", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        ExposedDropdownMenuBox(
                            expanded = isDropdownExpanded,
                            onExpandedChange = { isDropdownExpanded = it },
                            modifier = Modifier.width(100.dp) // Largura reduzida
                        ) {
                            OutlinedTextField(
                                value = maxMembers.toString(),
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                                modifier = Modifier.menuAnchor(),
                            )
                            ExposedDropdownMenu(
                                expanded = isDropdownExpanded,
                                onDismissRequest = { isDropdownExpanded = false }
                            ) {
                                memberOptions.forEach { number ->
                                    DropdownMenuItem(
                                        text = { Text(number.toString()) },
                                        onClick = {
                                            maxMembers = number
                                            isDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { if (it.length <= maxDescriptionChars) description = it },
                        label = { Text("Descrição") },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        supportingText = {
                            Text(text = "${description.length} / $maxDescriptionChars", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                        }
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { /* TODO: Lógica para criar o clube */ },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = homeButtonColor)
                    ) {
                        Text("CRIAR CLUBE", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showHelpDialog) {
        ModeHelpDialog(onDismiss = { showHelpDialog = false })
    }
}

// --- Componentes Auxiliares ---

@Composable
fun ModeSelector(selectedMode: ClubMode, onModeSelected: (ClubMode) -> Unit) {
    val cornerRadius = 50.dp // Raio para um formato de pílula

    Row(
        modifier = Modifier
            .fillMaxWidth(0.8f) // Ocupa 80% da largura para não ficar colado no '?'
            .height(48.dp)
            .clip(RoundedCornerShape(cornerRadius))
            .border(1.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(cornerRadius))
            .background(Color.LightGray.copy(alpha = 0.3f))
    ) {
        // Opção Privado
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(
                    color = if (selectedMode == ClubMode.PRIVATE) homeButtonColor else Color.Transparent,
                    shape = RoundedCornerShape(cornerRadius)
                )
                .clickable { onModeSelected(ClubMode.PRIVATE) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Privado",
                color = if (selectedMode == ClubMode.PRIVATE) Color.White else homeTextColor,
                fontWeight = FontWeight.Bold
            )
        }

        // Opção Público
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(
                    color = if (selectedMode == ClubMode.PUBLIC) homeButtonColor else Color.Transparent,
                    shape = RoundedCornerShape(cornerRadius)
                )
                .clickable { onModeSelected(ClubMode.PUBLIC) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Público",
                color = if (selectedMode == ClubMode.PUBLIC) Color.White else homeTextColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ModeHelpDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    "Modo privado:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    "usuários não são capazes de ver o clube na lista de procura. É necessário um convite ou inserir o código para participar.",
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                Text(
                    "Modo público:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    "usuários são capazes de ver o clube na lista de procura. Será necessário autorização do administrador para entrar.",
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("OK")
                }
            }
        }
    }
}


@Preview
@Composable
fun CreateClubScreenPreview() {
    LetrariaAppTheme {
        CreateClubScreen(onClose = {})
    }
}