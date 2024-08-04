package pt.isel.ls.player.domain

import kotlinx.serialization.Serializable

@Serializable
data class CreatePlayerInputModel(val name: String, val email: String, val password: String)
