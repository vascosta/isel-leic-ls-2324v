package pt.isel.ls.player.domain

data class Player(
    val id: Int,
    val name: String,
    val email: String,
    val password: String,
    val tokenHash: String?
)
