package br.com.letrariaapp.ui.features.createclub

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState // ✅ IMPORT ADICIONADO
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip // ✅ IMPORT ADICIONADO
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.letrariaapp.R
import br.com.letrariaapp.ui.components.AppBackground
import br.com.letrariaapp.ui.theme.ButtonRed
import br.com.letrariaapp.ui.theme.LetrariaAppTheme
import br.com.letrariaapp.ui.theme.TextColor
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateClubScreen(
    viewModel: CreateClubViewModel = viewModel(),
    onClose: () -> Unit
) {
    var clubName by remember { mutableStateOf("") }
    var isPublic by remember { mutableStateOf(false) } // Começa como privado por padrão
    var maxMembers by remember { mutableStateOf(10) }
    var description by remember { mutableStateOf("") }
    var showHelpDialog by remember { mutableStateOf(false) }

    val createClubResult by viewModel.createClubResult.observeAsState()
    val isLoading = createClubResult is CreateClubResult.LOADING

    CreateClubScreenContent(
        clubName = clubName, onClubNameChange = { clubName = it },
        isPublic = isPublic, onIsPublicChange = { isPublic = it },
        maxMembers = maxMembers, onMaxMembersChange = { maxMembers = it },
        description = description, onDescriptionChange = { description = it },
        onClose = onClose,
        onCreateClub = { viewModel.createClub(clubName, description, isPublic, maxMembers) },
        onShowHelp = { showHelpDialog = true },
        isLoading = isLoading
    )

    if (showHelpDialog) {
        ModeHelpDialog(onDismiss = { showHelpDialog = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateClubScreenContent(
    clubName: String, onClubNameChange: (String) -> Unit,
    isPublic: Boolean, onIsPublicChange: (Boolean) -> Unit,
    maxMembers: Int, onMaxMembersChange: (Int) -> Unit,
    description: String, onDescriptionChange: (String) -> Unit,
    onClose: () -> Unit,
    onCreateClub: () -> Unit,
    onShowHelp: () -> Unit,
    isLoading: Boolean
) {
    var isDropdownExpanded by remember { mutableStateOf(false) }
    val memberOptions = (2..20).toList()
    val maxDescriptionChars = 140

    AppBackground(backgroundResId = R.drawable.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                AsyncImage(
                    model = R.drawable.ic_launcher_background,
                    contentDescription = "Capa do Clube",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                IconButton(onClick = onClose, modifier = Modifier.align(Alignment.TopEnd).padding(8.dp), enabled = !isLoading) {
                    Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White)
                }
                TextButton(
                    onClick = { /* TODO: Upload */ },
                    modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                    enabled = !isLoading
                ) {
                    Text("Alterar Capa", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Column(modifier = Modifier.padding(24.dp)) {
                OutlinedTextField(
                    value = clubName,
                    onValueChange = onClubNameChange,
                    label = { Text("Nome") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading
                )
                Spacer(modifier = Modifier.height(24.dp))

                Column {
                    Text("Modo", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ModeSelector(
                            isPublic = isPublic,
                            onModeSelected = onIsPublicChange,
                            enabled = !isLoading
                        )
                        IconButton(onClick = onShowHelp) {
                            Icon(Icons.Default.Info, contentDescription = "Ajuda sobre os modos", tint = TextColor)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Número máximo de membros", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    ExposedDropdownMenuBox(
                        expanded = isDropdownExpanded,
                        onExpandedChange = { if (!isLoading) isDropdownExpanded = it },
                        modifier = Modifier.width(100.dp)
                    ) {
                        OutlinedTextField(
                            value = maxMembers.toString(),
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                            modifier = Modifier.menuAnchor(),
                            enabled = !isLoading
                        )
                        ExposedDropdownMenu(
                            expanded = isDropdownExpanded,
                            onDismissRequest = { isDropdownExpanded = false }
                        ) {
                            memberOptions.forEach { number ->
                                DropdownMenuItem(
                                    text = { Text(number.toString()) },
                                    onClick = {
                                        onMaxMembersChange(number)
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
                    onValueChange = { if (it.length <= maxDescriptionChars) onDescriptionChange(it) },
                    label = { Text("Descrição") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    supportingText = {
                        Text(text = "${description.length} / $maxDescriptionChars", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                    },
                    enabled = !isLoading
                )
                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onCreateClub,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ButtonRed),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text("CRIAR CLUBE", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ModeSelector(isPublic: Boolean, onModeSelected: (Boolean) -> Unit, enabled: Boolean) {
    val cornerRadius = 50.dp
    Row(
        modifier = Modifier
            .width(200.dp)
            .height(40.dp)
            .clip(RoundedCornerShape(cornerRadius))
            .border(1.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(cornerRadius))
    ) {
        Box(
            modifier = Modifier.weight(1f).fillMaxHeight()
                .background(
                    color = if (!isPublic) ButtonRed else Color.Transparent,
                    shape = RoundedCornerShape(cornerRadius)
                )
                .clickable(enabled = enabled) { onModeSelected(false) }, // Privado
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Privado", color = if (!isPublic) Color.White else TextColor, fontWeight = FontWeight.Bold)
        }
        Box(
            modifier = Modifier.weight(1f).fillMaxHeight()
                .background(
                    color = if (isPublic) ButtonRed else Color.Transparent,
                    shape = RoundedCornerShape(cornerRadius)
                )
                .clickable(enabled = enabled) { onModeSelected(true) }, // Público
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Público", color = if (isPublic) Color.White else TextColor, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ModeHelpDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Modo privado:", fontWeight = FontWeight.Bold)
                Text("Usuários não veem o clube na busca. É necessário um convite ou código para entrar.")
                Spacer(modifier = Modifier.height(16.dp))
                Text("Modo público:", fontWeight = FontWeight.Bold)
                Text("Usuários veem o clube na busca. Será necessário autorização do admin para entrar.")
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("OK")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateClubScreenContentPreview() {
    LetrariaAppTheme {
        CreateClubScreenContent(
            clubName = "Nome do Clube", onClubNameChange = {},
            isPublic = false, onIsPublicChange = {},
            maxMembers = 15, onMaxMembersChange = {},
            description = "Esta é uma descrição de exemplo para o clube.", onDescriptionChange = {},
            onClose = {},
            onCreateClub = {},
            onShowHelp = {},
            isLoading = false
        )
    }
}