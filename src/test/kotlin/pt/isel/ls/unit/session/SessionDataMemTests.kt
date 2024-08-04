package pt.isel.ls.unit.session

import org.junit.Before
import org.junit.Test
import pt.isel.ls.session.domain.SessionQuery
import pt.isel.ls.utils.PAGING_BUILT_IN_LIMIT
import pt.isel.ls.utils.Paging.Companion.DEFAULT_SKIP
import java.time.OffsetDateTime
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SessionDataMemTests : SessionBaseTest() {
    private val amountOfSessions = 30

    @Before
    fun `session template`() {
        repeat(amountOfSessions) {
            val session = sessionDataMem.createSession(Random.nextInt(2, CAPACITY), Random.nextInt(1, GAMED), DATE_NOW, HOST)
            assertNotNull(session)
            sessionIds.add(session.sessionId)
        }
    }

    @Test
    fun `creates session and succeeds`() {
        val session = sessionDataMem.createSession(CAPACITY, GAMED, DATE_NOW, HOST)

        assertNotNull(session)
        assertEquals(sessionIds.size + 1, session.sessionId)
    }

    @Test
    fun `deletes session and succeeds`() {
        val randomSession = sessionIds.random()
        val deletedSession = sessionDataMem.deleteSession(randomSession)

        assertTrue(deletedSession)

        val sessions = sessionDataMem.getSessions(SessionQuery(DEFAULT_SKIP, PAGING_BUILT_IN_LIMIT, hashMapOf()))

        assertEquals(sessionIds.size - 1, sessions.size)
        assertTrue(!sessions.any { it.id == randomSession })
    }

    @Test
    fun `adds a player to a session and succeeds`() {
        val randomSession = sessionIds.random()
        val playerToAdd = 5
        val isAdded = sessionDataMem.addPlayerToSession(randomSession, playerToAdd)

        assertTrue(isAdded)

        val sessionInfo = sessionDataMem.getSessionInfo(randomSession)

        assertNotNull(sessionInfo)
        assertNotNull(sessionInfo.players.firstOrNull { it.id == playerToAdd })
    }

    @Test
    fun `filters sessions and succeeds`() {
        val capacity = 4
        val date = OffsetDateTime.now()
        val sessionsToTest = 10
        val playerId = 69

        //  Multiple sessions with the same capacity, different gameIds, different dates
        //  Since they are all different, list sizes will be only 1
        repeat(sessionsToTest) {
            sessionDataMem.createSession(capacity, it + 1, date.plusHours((it + 1).toLong()), HOST)
        }

        //  2 sessions now have the same capacity and same gameId but different date
        val session = sessionDataMem.createSession(capacity, 1, date, HOST)

        //  Now that one has an extra player
        sessionDataMem.addPlayerToSession(session.sessionId, playerId)

        //  Since they all have different dates, it should only return the one with the player
        val sessionsByDate = sessionDataMem.getSessions(SessionQuery(DEFAULT_SKIP, PAGING_BUILT_IN_LIMIT, hashMapOf("date" to date.toString())))
        val sessionByDate = sessionsByDate.first()

        assertEquals(1, sessionsByDate.size)
        assertEquals(2, sessionByDate.players.size)
        assertNotNull(sessionByDate.players.firstOrNull { it.id == playerId })

        //  There is a single session with a player
        val sessionsByPlayer = sessionDataMem.getSessions(SessionQuery(DEFAULT_SKIP, PAGING_BUILT_IN_LIMIT, hashMapOf("pid" to "$playerId")))
        val sessionByPlayer = sessionsByPlayer.first()

        assertEquals(1, sessionsByPlayer.size)
        assertEquals(2, sessionByPlayer.players.size)
        assertNotNull(sessionByDate.players.firstOrNull { it.id == playerId })

        assertEquals(1, sessionByPlayer.game.id)
    }
}
