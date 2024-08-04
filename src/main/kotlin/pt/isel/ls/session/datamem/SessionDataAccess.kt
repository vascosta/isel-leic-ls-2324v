package pt.isel.ls.session.datamem

import pt.isel.ls.session.domain.SessionQuery
import pt.isel.ls.session.domain.models.input.SessionUpdateInputModel
import pt.isel.ls.session.domain.models.output.SessionDetailsOutputModel
import pt.isel.ls.session.domain.models.output.SessionIdentifierOutputModel
import java.time.OffsetDateTime

interface SessionDataAccess {
    fun createSession(capacity: Int, gameId: Int, date: OffsetDateTime, hostId: Int): SessionIdentifierOutputModel
    fun deleteSession(sessionId: Int): Boolean

    fun addPlayerToSession(sessionId: Int, playerId: Int): Boolean
    fun removePlayerFromSession(sessionId: Int, playerId: Int): Boolean

    fun getSessionInfo(sessionId: Int): SessionDetailsOutputModel?
    fun getSessions(sessionQuery: SessionQuery): List<SessionDetailsOutputModel>

    fun updateSession(sessionId: Int, updatedSession: SessionUpdateInputModel)

    fun isSessionFull(sessionId: Int): Boolean
    fun isSessionHost(sessionId: Int, playerId: Int): Boolean
    fun playerInSession(sessionId: Int, playerId: Int): Boolean
    fun sessionExists(sessionId: Int): Boolean
    fun isSessionExpired(sessionId: Int): Boolean
    fun isValidCapacity(sessionId: Int, capacity: Int?): Boolean
}
