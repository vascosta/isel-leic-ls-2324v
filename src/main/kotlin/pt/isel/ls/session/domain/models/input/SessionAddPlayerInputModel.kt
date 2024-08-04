package pt.isel.ls.session.domain.models.input

import kotlinx.serialization.Serializable

@Serializable
data class SessionAddPlayerInputModel(val sessionId: Int, val playerId: Int)
