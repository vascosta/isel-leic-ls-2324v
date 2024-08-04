package pt.isel.ls.game.domain

import kotlinx.serialization.Serializable

@Serializable
data class GameFiltersInputModel(
    val genres: List<Genres>?,
    val developer: String?,
    val name: String?
)
