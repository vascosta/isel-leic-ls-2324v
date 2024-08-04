package pt.isel.ls.session.domain.models.output

import kotlinx.serialization.Serializable
import pt.isel.ls.game.domain.GameIdentificationOutputModel

@Serializable
data class SessionDetailsOutputModel(
    val id: Int,
    val capacity: Int,
    val date: String,
    val game: GameIdentificationOutputModel,
    val players: List<SessionPlayerOutputModel>,
    val hostId: Int
)
