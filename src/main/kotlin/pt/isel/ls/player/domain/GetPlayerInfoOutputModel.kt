package pt.isel.ls.player.domain

import kotlinx.serialization.Serializable

@Serializable
data class GetPlayerInfoOutputModel(val id: Int, val name: String, val email: String)
