package pt.isel.ls.session.domain

import kotlinx.serialization.Serializable
import java.time.OffsetDateTime

@Serializable
data class Session(
    val id: Int,
    val capacity: Int,
    @Serializable(with = OffsetDateTimeSerializer::class)
    val date: OffsetDateTime,
    val gameId: Int,
    val players: List<Int>,
    val hostId: Int
)
