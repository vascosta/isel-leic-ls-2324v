package pt.isel.ls.game.domain

import kotlinx.serialization.Serializable

@Serializable
data class CreateGameInputModel(
    val name: String,
    val developer: String,
    val genres: List<Genres>
)
