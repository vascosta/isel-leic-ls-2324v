package pt.isel.ls.player.domain

import kotlinx.serialization.Serializable

@Serializable
data class LoginInputModel(
    val name: String,
    val password: String
)
