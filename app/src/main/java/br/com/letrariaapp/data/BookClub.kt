package br.com.letrariaapp.data

data class BookClub(
    val id: String,
    val name: String,
    val imageUrl: String,
    val currentMembers: Int,
    val maxMembers: Int
)