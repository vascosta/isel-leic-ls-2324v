package pt.isel.ls.game.domain

import kotlinx.serialization.Serializable

@Serializable
data class GameListOutputModel(
    val games: List<GameOutputModel>
)
