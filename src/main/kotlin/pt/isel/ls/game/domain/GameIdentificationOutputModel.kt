package pt.isel.ls.game.domain

import kotlinx.serialization.Serializable

@Serializable
data class GameIdentificationOutputModel(
    val id: Int,
    val game: String
)
