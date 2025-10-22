package br.com.letrariaapp.data

data class BookClub(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val isPublic: Boolean = true,
    val code: String? = null,
    val adminId: String = "",
    val members: List<String> = emptyList(),
    val maxMembers: Int = 10,
    val readingCycleDays: Int = 15,

    val currentUserForCycleId: String? = null,
    val indicatedBookTitle: String? = null,
    val cycleEndDate: Long? = null,

    // ✅ NOVO CAMPO ADICIONADO
    val joinRequests: List<String> = emptyList(), // Lista de User IDs que pediram para entrar

    val createdAt: Long = System.currentTimeMillis()
)