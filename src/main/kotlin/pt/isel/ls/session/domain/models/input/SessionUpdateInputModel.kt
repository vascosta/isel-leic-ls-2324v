package pt.isel.ls.session.domain.models.input

import kotlinx.serialization.Serializable
import pt.isel.ls.session.domain.OffsetDateTimeSerializer
import java.time.OffsetDateTime

@Serializable
data class SessionUpdateInputModel(
    val capacity: Int? = null,
    val gameId: Int? = null,
    @Serializable(with = OffsetDateTimeSerializer::class)
    val date: OffsetDateTime? = null
)
