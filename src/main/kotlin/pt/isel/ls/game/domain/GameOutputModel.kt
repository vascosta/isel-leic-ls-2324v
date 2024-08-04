package pt.isel.ls.game.domain

import kotlinx.serialization.Serializable

@Serializable
data class GameOutputModel(
    val id: Int,
    val name: String,
    val developer: String,
    val genres: List<Genres>
)

fun String.toOriginalName() = this.replace("-", " ")
