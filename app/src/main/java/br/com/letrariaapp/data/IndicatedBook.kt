package br.com.letrariaapp.data

// Molde para o livro que foi indicado
data class IndicatedBook(
    val title: String = "",
    val author: String = "",
    val publisher: String = "",
    val coverUrl: String = "" // Vamos preencher com um padrão por enquanto
)