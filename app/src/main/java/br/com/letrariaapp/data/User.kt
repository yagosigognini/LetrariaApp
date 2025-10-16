package br.com.letrariaapp.data

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val profilePictureUrl: String = "",
    val aboutMe: String = "",
    val friendCount: Long = 0, // Firestore prefere Long para números
    val checklist: List<RatedBook> = emptyList(),
    val clubs: List<BookClub> = emptyList()
)