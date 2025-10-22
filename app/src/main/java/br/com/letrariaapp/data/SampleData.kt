package br.com.letrariaapp.data

import br.com.letrariaapp.R

// --- DADOS DE EXEMPLO PARA O USUÁRIO ---
val sampleUser = User(
    uid = "123",
    name = "Clarice Lispector",
    email = "clarice@exemplo.com",
    aboutMe = "Escritora e jornalista, considerada uma das mais importantes do século XX.",
    profilePictureUrl = "https://picsum.photos/seed/clarice/200",
    friendCount = 67
)

// --- DADOS DE EXEMPLO PARA A HOME SCREEN ---

val sampleQuote = Quote(
    text = "\"Tu te tornas eternamente responsável por aquilo que cativas.\"",
    source = "O Pequeno Príncipe – Antoine de Saint-Exupéry"
)

// --- DADOS DE EXEMPLO PARA OS CLUBES ---
val sampleClubsList = listOf(
    BookClub(
        id = "1",
        name = "Clássicos da Meia-Noite",
        imageUrl = "https://picsum.photos/seed/101/400/200",
        members = List(7) { "" }, // Usando o nome correto "members"
        maxMembers = 15
    ),
    BookClub(
        id = "2",
        name = "Terror & Suspense",
        imageUrl = "https://picsum.photos/seed/102/400/200",
        members = List(12) { "" },
        maxMembers = 20
    ),
    BookClub(
        id = "3",
        name = "Aventura Fantástica",
        imageUrl = "https://picsum.photos/seed/103/400/200",
        members = List(5) { "" },
        maxMembers = 10
    )
)