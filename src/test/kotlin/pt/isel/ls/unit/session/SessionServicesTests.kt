package pt.isel.ls.unit.session

import junit.framework.TestCase.assertTrue
import org.junit.Test
import pt.isel.ls.session.datamem.SessionDataMem
import pt.isel.ls.session.domain.SessionQuery
import pt.isel.ls.session.domain.models.input.SessionUpdateInputModel
import pt.isel.ls.session.services.SessionServices
import pt.isel.ls.utils.InvalidCapacityException
import pt.isel.ls.utils.InvalidDateException
import pt.isel.ls.utils.InvalidGameIdException
import pt.isel.ls.utils.InvalidPlayerIdException
import pt.isel.ls.utils.InvalidSessionIdException
import pt.isel.ls.utils.PAGING_BUILT_IN_LIMIT
import pt.isel.ls.utils.Paging.Companion.DEFAULT_SKIP
import pt.isel.ls.utils.PlayerNotInSessionException
import pt.isel.ls.utils.PlayerNotTheSessionHostException
import pt.isel.ls.utils.SessionDoesntExistException
import pt.isel.ls.utils.SessionHasPlayerException
import pt.isel.ls.utils.SessionHostCantBeRemovedException
import pt.isel.ls.utils.SessionIsFullException
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class SessionServicesTests {

    private val services = SessionServices(SessionDataMem())

    @Test
    fun `creates a session and succeeds`() {
        val session = services.createSession(DATE, GAMEID, CAPACITY, HOSTID)

        assertNotNull(session)
        assertEquals(1, session.sessionId)
    }

    @Test
    fun `creates a session and fails with InvalidExceptions`() {
        val invalidDate = OffsetDateTime.of(-2003, 3, 20, 10, 10, 0, 0, ZoneOffset.UTC)
        val invalidGameId = -1
        val invalidCapacity = -4

        assertFailsWith<InvalidDateException> { services.createSession(invalidDate, GAMEID, CAPACITY, HOSTID) }
        assertFailsWith<InvalidGameIdException> { services.createSession(DATE, invalidGameId, CAPACITY, HOSTID) }
        assertFailsWith<InvalidCapacityException> { services.createSession(DATE, GAMEID, invalidCapacity, HOSTID) }
    }

    @Test
    fun `deletes a session and succeeds`() {
        val session = services.createSession(DATE, GAMEID, CAPACITY, HOSTID)

        assertNotNull(session)
        assertEquals(1, session.sessionId)

        val removed = services.deleteSession(session.sessionId, HOSTID)

        assertTrue(removed)
    }

    @Test
    fun `adds a player and succeeds`() {
        val session = services.createSession(DATE, GAMEID, CAPACITY, HOSTID)

        assertNotNull(session)
        assertEquals(1, session.sessionId)

        val playerId = 2
        val isAdded = services.addPlayerToSession(session.sessionId, playerId)

        assertTrue(isAdded)
    }

    @Test
    fun `adds a player to an invalid session and fails with InvalidSessionException`() {
        val sessionId = -1
        val playerId = 1

        assertFailsWith<InvalidSessionIdException> { services.addPlayerToSession(sessionId, playerId) }
    }

    @Test
    fun `adds an invalid player to a valid session and fails with InvalidPlayerIdException`() {
        val sessionId = 1
        val playerId = -1

        assertFailsWith<InvalidPlayerIdException> { services.addPlayerToSession(sessionId, playerId) }
    }

    @Test
    fun `adds a valid player to a valid session but doesnt exist and fails with SessionDoesntExistException`() {
        val sessionId = 3
        val playerId = 1

        assertFailsWith<SessionDoesntExistException> { services.addPlayerToSession(sessionId, playerId) }
    }

    @Test
    fun `adds a valid player to a full session and fails with SessionIsFullException`() {
        val session = services.createSession(DATE, GAMEID, CAPACITY, HOSTID)
        repeat(CAPACITY - 1) {
            services.addPlayerToSession(session.sessionId, it + 2)
        }
        val playerId = 10
        assertFailsWith<SessionIsFullException> { services.addPlayerToSession(session.sessionId, playerId) }
    }

    @Test
    fun `gets info of a session and succeeds`() {
        val sessionId = services.createSession(DATE, GAMEID, CAPACITY, HOSTID)
        val session = services.getSessionInfo(sessionId.sessionId)

        assertNotNull(session)
    }

    @Test
    fun `gets info of an invalid session and fails with InvalidSessionIdException`() {
        val sessionId = -1

        assertFailsWith<InvalidSessionIdException> { services.getSessionInfo(sessionId) }
    }

    @Test
    fun `gets sessions and succeeds`() {
        for (gameId in 1..5) {
            services.createSession(DATE, gameId, CAPACITY, HOSTID)
        }

        val session = services.getSessions(SessionQuery(DEFAULT_SKIP, PAGING_BUILT_IN_LIMIT, hashMapOf()))

        assertNotNull(session)
        assertEquals(5, session.size)

        val sessions = services.getSessions(SessionQuery(DEFAULT_SKIP, PAGING_BUILT_IN_LIMIT, hashMapOf("gid" to "1")))

        assertNotNull(sessions)
        assertEquals(1, sessions.size)
    }

    @Test
    fun `adds player to a session with success`() {
        val session = services.createSession(DATE, GAMEID, CAPACITY, HOSTID)
        val addedPlayer = services.addPlayerToSession(session.sessionId, GUESTID)
        assertTrue(addedPlayer)
    }

    @Test
    fun `tries to add a player to a session that doesnt exist and fails with SessionDoesntExistException `() {
        assertFailsWith<SessionDoesntExistException> { services.addPlayerToSession(1, GUESTID) }
    }

    @Test
    fun `tries to add a player to a session that is full and fails with SessionIsFullException`() {
        val session = services.createSession(DATE, GAMEID, CAPACITY, HOSTID)
        repeat(CAPACITY - 1) { services.addPlayerToSession(session.sessionId, it + 2) }
        assertFailsWith<SessionIsFullException> { services.addPlayerToSession(session.sessionId, GUESTID) }
    }

    @Test
    fun `tries to remove a player from a session and succeeds`() {
        val session = services.createSession(DATE, GAMEID, CAPACITY, HOSTID)
        val isAdded = services.addPlayerToSession(session.sessionId, GUESTID)
        assertTrue(isAdded)

        val isRemoved = services.removePlayerFromSession(session.sessionId, GUESTID)
        assertTrue(isRemoved)
    }

    @Test
    fun `tries to remove a player from a session that doesnt exist and fails with SessionDoesntExist`() {
        assertFailsWith<SessionDoesntExistException> { services.removePlayerFromSession(5, GUESTID) }
    }

    @Test
    fun `tries to remove a player from a session where hes not and fails with PlayerNotInSessionException `() {
        val session = services.createSession(DATE, GAMEID, CAPACITY, HOSTID)
        assertFailsWith<PlayerNotInSessionException> { services.removePlayerFromSession(session.sessionId, GUESTID) }
    }

    @Test
    fun `tries to remove a host from a session and fails with SessionHostCantBeRemovedException`() {
        val session = services.createSession(DATE, GAMEID, CAPACITY, HOSTID)
        assertFailsWith<SessionHostCantBeRemovedException> { services.removePlayerFromSession(session.sessionId, HOSTID) }
    }

    @Test
    fun `tries to add a player to a session that already has the player and fails with SessionHasPlayerException `() {
        val session = services.createSession(DATE, GAMEID, CAPACITY, HOSTID)
        services.addPlayerToSession(session.sessionId, GUESTID)
        assertFailsWith<SessionHasPlayerException> { services.addPlayerToSession(1, GUESTID) }
    }

    @Test
    fun `tries to update a session and succeeds`() {
        val session = services.createSession(DATE, GAMEID, CAPACITY, HOSTID)
        val updatedSession = SessionUpdateInputModel(CAPACITY + 1, GAMEID + 1, null)
        services.updateSession(session.sessionId, HOSTID, updatedSession)

        val newSession = services.getSessionInfo(session.sessionId)
        assertEquals(CAPACITY + 1, newSession.capacity)
        assertEquals(GAMEID + 1, newSession.game.id)
    }

    @Test
    fun `tries to update a session that doesnt exist and fails with SessionDoesntExistException`() {
        val updatedSession = SessionUpdateInputModel(CAPACITY + 1, GAMEID + 1, null)
        assertFailsWith<SessionDoesntExistException> { services.updateSession(1, HOSTID, updatedSession) }
    }

    @Test
    fun `tries to update a session not being the host and fails with PlayerNotTheSessionHostException`() {
        val session = services.createSession(DATE, GAMEID, CAPACITY, HOSTID)
        val updatedSession = SessionUpdateInputModel(CAPACITY + 1, GAMEID + 1, null)
        assertFailsWith<PlayerNotTheSessionHostException> { services.updateSession(session.sessionId, HOSTID + 1, updatedSession) }
    }

    companion object {
        const val CAPACITY = 4
        const val GAMEID = 2
        val DATE = OffsetDateTime.now().plusHours(1)
        const val HOSTID = 1
        const val GUESTID = 40
    }
}
