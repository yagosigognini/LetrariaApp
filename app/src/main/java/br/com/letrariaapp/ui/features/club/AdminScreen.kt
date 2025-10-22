package br.com.letrariaapp.ui.features.club

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.letrariaapp.R
import br.com.letrariaapp.data.BookClub
import br.com.letrariaapp.data.User
import br.com.letrariaapp.data.sampleClubsList // ✅ IMPORT ADICIONADO
import br.com.letrariaapp.data.sampleUser
import br.com.letrariaapp.ui.components.AppBackground
import br.com.letrariaapp.ui.theme.LetrariaAppTheme
import coil.compose.AsyncImage

@Composable
fun AdminScreen(
    clubId: String,
    viewModel: AdminViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    LaunchedEffect(clubId) {
        viewModel.loadAdminData(clubId)
    }

    val club by viewModel.club.observeAsState()
    val requests by viewModel.requests.observeAsState(emptyList())
    val members by viewModel.members.observeAsState(emptyList())
    val isLoadingRequests by viewModel.isLoadingRequests.observeAsState(false)
    val isLoadingMembers by viewModel.isLoadingMembers.observeAsState(false)

    AdminScreenContent(
        club = club,
        requests = requests,
        members = members,
        isLoadingRequests = isLoadingRequests,
        isLoadingMembers = isLoadingMembers,
        onBackClick = onBackClick,
        onApprove = { request -> viewModel.approveRequest(request.userId) },
        onDeny = { request -> viewModel.denyRequest(request.userId) },
        onKick = { member -> viewModel.kickMember(member.uid) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreenContent(
    club: BookClub?,
    requests: List<JoinRequest>,
    members: List<User>,
    isLoadingRequests: Boolean,
    isLoadingMembers: Boolean,
    onBackClick: () -> Unit,
    onApprove: (JoinRequest) -> Unit,
    onDeny: (JoinRequest) -> Unit,
    onKick: (User) -> Unit
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Solicitações", "Membros", "Configurações")

    AppBackground(backgroundResId = R.drawable.background) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Painel do Admin") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
                TabRow(selectedTabIndex = selectedTabIndex) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }

                when (selectedTabIndex) {
                    0 -> RequestsTab(requests, isLoadingRequests, onApprove, onDeny)
                    1 -> MembersTab(members, isLoadingMembers, club?.adminId, onKick)
                    2 -> ConfigTab(club)
                }
            }
        }
    }
}

// --- ABAS ---
@Composable
fun RequestsTab(
    requests: List<JoinRequest>,
    isLoading: Boolean,
    onApprove: (JoinRequest) -> Unit,
    onDeny: (JoinRequest) -> Unit
) {
    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (requests.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Nenhuma solicitação pendente.", modifier = Modifier.padding(16.dp))
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(requests) { request ->
                RequestItem(
                    user = request.user,
                    onApprove = { onApprove(request) },
                    onDeny = { onDeny(request) }
                )
            }
        }
    }
}

@Composable
fun MembersTab(
    members: List<User>,
    isLoading: Boolean,
    adminId: String?,
    onKick: (User) -> Unit
) {
    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(members) { user ->
                MemberItem(
                    user = user,
                    isAdmin = user.uid == adminId,
                    onKick = { onKick(user) }
                )
            }
        }
    }
}

@Composable
fun ConfigTab(club: BookClub?) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (club == null) {
            CircularProgressIndicator()
            return
        }

        if (club.code != null) {
            Text("Configurações do Clube", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = club.code,
                onValueChange = {},
                label = { Text("Código de Convite") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text("Este é um clube público e não possui código de convite.")
        }
    }
}

// --- COMPONENTES DAS ABAS ---
@Composable
fun RequestItem(
    user: User,
    onApprove: () -> Unit,
    onDeny: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = user.profilePictureUrl.ifEmpty { R.drawable.ic_launcher_background },
                contentDescription = "Foto de ${user.name}",
                modifier = Modifier.size(50.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(user.name, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)

            TextButton(onClick = onDeny) {
                Text("Negar")
            }
            Button(onClick = onApprove) {
                Text("Aprovar")
            }
        }
    }
}

@Composable
fun MemberItem(
    user: User,
    isAdmin: Boolean,
    onKick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = user.profilePictureUrl.ifEmpty { R.drawable.ic_launcher_background },
                contentDescription = "Foto de ${user.name}",
                modifier = Modifier.size(50.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(user.name, fontWeight = FontWeight.Bold)
                if(isAdmin) {
                    Text("Admin", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }

            if (!isAdmin) {
                TextButton(onClick = onKick) {
                    Text("Expulsar", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

// ✅ PREVIEW CORRIGIDO
@Preview(showBackground = true)
@Composable
fun AdminScreenPreview() {
    val requests = listOf(
        JoinRequest("1", sampleUser.copy(name = "Usuário 1")),
        JoinRequest("2", sampleUser.copy(name = "Usuário 2"))
    )
    val members = listOf(
        sampleUser.copy(uid = "admin_id", name = "Admin do Clube"),
        sampleUser.copy(uid = "3", name = "Membro 1")
    )
    val club = sampleClubsList.first().copy(adminId = "admin_id")

    LetrariaAppTheme {
        AdminScreenContent(
            club = club,
            requests = requests,
            members = members,
            isLoadingRequests = false,
            isLoadingMembers = false,
            onBackClick = {},
            onApprove = {},
            onDeny = {},
            onKick = {}
        )
    }
}