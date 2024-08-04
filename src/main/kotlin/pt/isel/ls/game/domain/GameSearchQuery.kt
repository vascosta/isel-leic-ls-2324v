package pt.isel.ls.game.domain

import pt.isel.ls.utils.Paging

data class GameSearchQuery(
    override val skip: Int,
    override val limit: Int,
    val genres: List<Genres>?,
    val developer: String?,
    val name: String?
) : Paging
