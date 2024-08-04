package pt.isel.ls.session.domain.models.input

import kotlinx.serialization.Serializable
import pt.isel.ls.session.domain.OffsetDateTimeSerializer
import java.time.OffsetDateTime

@Serializable
data class SessionCreateInputModel(
    val capacity: Int,
    val gameId: Int,
    @Serializable(with = OffsetDateTimeSerializer::class)
    val date: OffsetDateTime
)
