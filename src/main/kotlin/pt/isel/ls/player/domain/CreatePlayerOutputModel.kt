package pt.isel.ls.player.domain

import kotlinx.serialization.Serializable

@Serializable
data class CreatePlayerOutputModel(val token: String, val id: Int)

typealias LoginOutputModel = CreatePlayerOutputModel
