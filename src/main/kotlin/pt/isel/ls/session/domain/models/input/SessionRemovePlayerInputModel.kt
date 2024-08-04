package pt.isel.ls.session.domain.models.input

import kotlinx.serialization.Serializable

@Serializable
data class SessionRemovePlayerInputModel(val playerId: Int)
