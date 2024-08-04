package pt.isel.ls.player.domain

import kotlinx.serialization.Serializable

@Serializable
data class SearchPlayersByNameOutputModel(
    val players: List<GetPlayerInfoOutputModel>
)
