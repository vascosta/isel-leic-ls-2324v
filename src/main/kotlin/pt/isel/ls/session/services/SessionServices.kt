package pt.isel.ls.session.services

import pt.isel.ls.session.datamem.SessionDataAccess
import pt.isel.ls.session.domain.SessionQuery
import pt.isel.ls.session.domain.models.input.SessionUpdateInputModel
import pt.isel.ls.session.domain.models.output.SessionDetailsOutputModel
import pt.isel.ls.session.domain.models.output.SessionIdentifierOutputModel
import pt.isel.ls.session.utils.checkCapacity
import pt.isel.ls.session.utils.checkDate
import pt.isel.ls.session.utils.checkGameId
import pt.isel.ls.session.utils.checkOptionals
import pt.isel.ls.session.utils.checkPlayerId
import pt.isel.ls.session.utils.checkSessionId
import pt.isel.ls.utils.InvalidCapacityException
import pt.isel.ls.utils.PlayerNotInSessionException
import pt.isel.ls.utils.PlayerNotTheSessionHostException
import pt.isel.ls.utils.SessionDoesntExistException
import pt.isel.ls.utils.SessionHasExpiredException
import pt.isel.ls.utils.SessionHasPlayerException
import pt.isel.ls.utils.SessionHostCantBeRemovedException
import pt.isel.ls.utils.SessionIsFullException
import java.time.OffsetDateTime

class SessionServices(private val dataMem: SessionDataAccess) {

    fun createSession(date: OffsetDateTime, gameId: Int, capacity: Int, hostId: Int): SessionIdentifierOutputModel {
        checkDate(date)
        checkGameId(gameId)
        checkCapacity(capacity)
        checkPlayerId(hostId)
        return dataMem.createSession(capacity, gameId, date, hostId)
    }

    fun deleteSession(sessionId: Int, hostId: Int): Boolean {
        checkSessionId(sessionId)
        if (!dataMem.sessionExists(sessionId)) throw SessionDoesntExistException
        if (!dataMem.isSessionHost(sessionId, hostId)) throw PlayerNotTheSessionHostException
        return dataMem.deleteSession(sessionId)
    }

    fun addPlayerToSession(sessionId: Int, playerId: Int): Boolean {
        checkSessionId(sessionId)
        checkPlayerId(playerId)
        if (!dataMem.sessionExists(sessionId)) throw SessionDoesntExistException
        if (dataMem.isSessionExpired(sessionId)) throw SessionHasExpiredException
        if (dataMem.isSessionFull(sessionId)) throw SessionIsFullException
        if (dataMem.playerInSession(sessionId, playerId)) throw SessionHasPlayerException
        return dataMem.addPlayerToSession(sessionId, playerId)
    }

    fun removePlayerFromSession(sessionId: Int, playerId: Int): Boolean {
        checkSessionId(sessionId)
        checkPlayerId(playerId)
        if (!dataMem.sessionExists(sessionId)) throw SessionDoesntExistException
        if (dataMem.isSessionExpired(sessionId)) throw SessionHasExpiredException
        if (!dataMem.playerInSession(sessionId, playerId)) throw PlayerNotInSessionException
        if (dataMem.isSessionHost(sessionId, playerId)) throw SessionHostCantBeRemovedException
        return dataMem.removePlayerFromSession(sessionId, playerId)
    }

    fun getSessionInfo(sessionId: Int): SessionDetailsOutputModel {
        checkSessionId(sessionId)
        return dataMem.getSessionInfo(sessionId) ?: throw SessionDoesntExistException
    }

    fun getSessions(optionals: SessionQuery): List<SessionDetailsOutputModel> {
        checkOptionals(optionals.optionals)
        return dataMem.getSessions(optionals)
    }

    fun updateSession(sessionId: Int, hostId: Int, updatedSession: SessionUpdateInputModel) {
        checkSessionId(sessionId)
        if (!dataMem.sessionExists(sessionId)) throw SessionDoesntExistException
        if (dataMem.isSessionExpired(sessionId)) throw SessionHasExpiredException
        if (!dataMem.isSessionHost(sessionId, hostId)) throw PlayerNotTheSessionHostException
        if (updatedSession.capacity != null) checkCapacity(updatedSession.capacity)
        if (!dataMem.isValidCapacity(sessionId, updatedSession.capacity)) throw InvalidCapacityException
        if (updatedSession.date != null) checkDate(updatedSession.date)
        if (updatedSession.gameId != null) checkGameId(updatedSession.gameId)
        return dataMem.updateSession(sessionId, updatedSession)
    }
}
