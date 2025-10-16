package br.com.letrariaapp.data

val sampleQuote = Quote(
    text = "\"Tu te tornas eternamente responsável por aquilo que cativas.\"",
    source = "O Pequeno Príncipe – Antoine de Saint-Exupéry"
)

val sampleClubsList = listOf(
    BookClub(id = "1", name = "Clássicos da Ficção", imageUrl = "https://i.imgur.com/sI22NM8.jpeg", currentMembers = 7, maxMembers = 15),
    BookClub(id = "2", name = "Terror e Suspense", imageUrl = "https://i.imgur.com/EZ919sM.jpeg", currentMembers = 12, maxMembers = 15),
    BookClub(id = "3", name = "Fantasias Épicas", imageUrl = "https://i.imgur.com/83oA79G.jpeg", currentMembers = 5, maxMembers = 15)
)

val sampleUser = User(
    uid = "1",
    name = "Clarice Lispector",
    email = "clarice@exemplo.com",
    profilePictureUrl = "https://i.imgur.com/gC52M5f.jpeg",
    aboutMe = "Escritora e jornalista ucraniana de origem judaica russa, naturalizada brasileira e radicada no Brasil.",
    friendCount = 67,
    // --- CORREÇÃO AQUI ---
    // Usando parâmetros nomeados: book = Book(...) e rating = ...
    checklist = listOf(
        RatedBook(book = Book(id = "c1", title = "A Hora da Estrela", author = "Clarice Lispector", coverUrl = "https://i.imgur.com/u382xNp.jpeg"), rating = 4),
        RatedBook(book = Book(id = "c2", title = "A Paixão Segundo G.H.", author = "Clarice Lispector", coverUrl = "https://i.imgur.com/sI22NM8.jpeg"), rating = 5),
        RatedBook(book = Book(id = "c3", title = "Perto do Coração Selvagem", author = "Clarice Lispector", coverUrl = "https://i.imgur.com/EZ919sM.jpeg"), rating = 4)
    ),
    clubs = sampleClubsList
)