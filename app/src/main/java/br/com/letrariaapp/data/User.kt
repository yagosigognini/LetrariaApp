package br.com.letrariaapp.data

data class User(
    val id: String,
    val name: String,
    val profilePictureUrl: String,
    val friendCount: Int,
    val aboutMe: String,
    val checklist: List<RatedBook>,
    val clubs: List<BookClub>
)