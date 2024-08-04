package pt.isel.ls.session.datamem

import pt.isel.ls.game.domain.GameIdentificationOutputModel
import pt.isel.ls.session.domain.Session
import pt.isel.ls.session.domain.SessionQuery
import pt.isel.ls.session.domain.models.input.SessionUpdateInputModel
import pt.isel.ls.session.domain.models.output.SessionDetailsOutputModel
import pt.isel.ls.session.domain.models.output.SessionIdentifierOutputModel
import pt.isel.ls.session.domain.models.output.SessionPlayerOutputModel
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.min

class SessionDataMem : SessionDataAccess {

    private val sessions = ConcurrentHashMap<Int, Session>()

    override fun createSession(capacity: Int, gameId: Int, date: OffsetDateTime, hostId: Int): SessionIdentifierOutputModel {
        val id = sessions.size + 1
        sessions[id] = Session(id, capacity, date, gameId, listOf(hostId), hostId)
        return SessionIdentifierOutputModel(id)
    }

    override fun deleteSession(sessionId: Int): Boolean = sessions.remove(sessionId) != null

    override fun addPlayerToSession(sessionId: Int, playerId: Int): Boolean {
        val session = sessions[sessionId] ?: return false
        val updatedPlayers = session.players + playerId
        sessions[sessionId] = session.copy(players = updatedPlayers)
        return true
    }

    override fun removePlayerFromSession(sessionId: Int, playerId: Int): Boolean {
        val session = sessions[sessionId] ?: return false
        val updatedPlayers = session.players - playerId
        sessions[sessionId] = session.copy(players = updatedPlayers)
        return true
    }

    override fun getSessionInfo(sessionId: Int): SessionDetailsOutputModel? {
        val session = sessions[sessionId] ?: return null
        val blankPlayers = session.players.map { playerId ->
            SessionPlayerOutputModel(playerId, "")
        }
        return SessionDetailsOutputModel(
            sessionId,
            session.capacity,
            session.date.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
            GameIdentificationOutputModel(session.gameId, ""),
            blankPlayers,
            session.hostId
        )
    }

    override fun getSessions(sessionQuery: SessionQuery): List<SessionDetailsOutputModel> {
        val filteredSessions = sessions.filter { (_, session) ->
            sessionQuery.optionals.all { (key, value) ->
                sessionMatchesQueryParam(session, key, value)
            }
        }.values.toList()

        val startIndex = min(sessionQuery.skip, filteredSessions.size)
        val endIndex = min(sessionQuery.skip + sessionQuery.limit, filteredSessions.size)

        return filteredSessions.subList(startIndex, endIndex).map {
            val blankPlayers = it.players.map { playerId ->
                SessionPlayerOutputModel(playerId, "")
            }
            SessionDetailsOutputModel(
                it.id,
                it.capacity,
                it.date.toString(),
                GameIdentificationOutputModel(it.gameId, ""),
                blankPlayers,
                it.hostId
            )
        }
    }

    override fun updateSession(sessionId: Int, updatedSession: SessionUpdateInputModel) {
        val session = sessions[sessionId]
        if (session != null) { // session is never null because the function "sessionExists" is called before in the services
            val updatedCapacity = updatedSession.capacity ?: session.capacity
            val updatedDate = updatedSession.date ?: session.date
            val updatedGameId = updatedSession.gameId ?: session.gameId
            sessions[sessionId] = session.copy(
                capacity = updatedCapacity,
                date = updatedDate,
                gameId = updatedGameId
            )
        }
    }

    override fun isSessionFull(sessionId: Int): Boolean {
        return sessions[sessionId]?.players?.size == sessions[sessionId]?.capacity
    }

    override fun isSessionHost(sessionId: Int, playerId: Int): Boolean {
        return sessions[sessionId]?.hostId == playerId
    }

    override fun playerInSession(sessionId: Int, playerId: Int): Boolean {
        return sessions[sessionId]?.players?.contains(playerId) ?: false
    }

    override fun sessionExists(sessionId: Int): Boolean {
        return sessions[sessionId] != null
    }

    override fun isSessionExpired(sessionId: Int): Boolean {
        return sessions[sessionId]!!.date < OffsetDateTime.now()
    }

    override fun isValidCapacity(sessionId: Int, capacity: Int?): Boolean {
        return capacity != null && capacity >= sessions[sessionId]!!.players.size
    }

    private fun sessionMatchesQueryParam(session: Session, paramName: String, paramValue: String): Boolean {
        return when (paramName) {
            "gid" -> session.gameId == paramValue.toIntOrNull()
            "date" -> session.date == OffsetDateTime.parse(paramValue)
            "state" -> (session.players.size == session.capacity) == paramValue.toBoolean()
            "pid" -> session.players.contains(paramValue.toIntOrNull())
            else -> false
        }
    }
}
