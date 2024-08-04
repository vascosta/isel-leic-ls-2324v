package pt.isel.ls.player.domain

import kotlinx.serialization.Serializable

@Serializable
data class GetPlayerHomeOutputModel(
    val id: Int,
    val name: String,
)
