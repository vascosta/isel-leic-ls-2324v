package pt.isel.ls.integration

import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.CREATED
import org.http4k.core.Status.Companion.FORBIDDEN
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.UNAUTHORIZED
import pt.isel.ls.game.domain.GameIdOutputModel
import pt.isel.ls.session.domain.Session
import pt.isel.ls.session.domain.models.output.SessionDetailsOutputModel
import pt.isel.ls.session.domain.models.output.SessionIdentifierOutputModel
import pt.isel.ls.utils.createAndGetPlayerToken
import pt.isel.ls.utils.getBody
import java.time.OffsetDateTime
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import pt.isel.ls.utils.addPlayerToSessionRequest
import pt.isel.ls.utils.createSessionRequest
import pt.isel.ls.utils.deleteSessionRequest
import pt.isel.ls.utils.removePlayerFromSessionRequest
import pt.isel.ls.utils.updateSessionRequest
import java.time.format.DateTimeFormatter

class SessionIntegrationTests: IntegrationBaseTest() {

    @Test
    fun `creates session and succeeds with 201`() {
        val createSession = createSessionRequest(CAPACITY, gameId, DATE, hostId, hostToken)
        val createSessionResponse = api.sessionApi.routes(createSession)
        println(createSessionResponse.bodyString())
        val body = getBody<SessionIdentifierOutputModel>(createSessionResponse.bodyString())

        assertEquals(CREATED, createSessionResponse.status)
        assertNotNull(body)
    }

    @Test
    fun `adds a player to a session and succeeds with 200`() {
        val addPlayer = addPlayerToSessionRequest(sessionIds.random(), guestId, guestToken)
        val addPlayerResponse = api.sessionApi.routes(addPlayer)

        assertEquals(OK, addPlayerResponse.status)
    }

    @Test
    fun `adds a player to a session that doesnt exist and fails with 404`() {
        val addPlayer = addPlayerToSessionRequest(999999, guestId, guestToken)
        val addPlayerResponse = api.sessionApi.routes(addPlayer)

        assertEquals(NOT_FOUND, addPlayerResponse.status)
    }

    @Test
    fun `gets session info and succeeds with 200 and returns the Session`() {
        val randomSession = sessionIds.random()
        val getSession = Request(GET, "/session/info/$randomSession")
        val getSessionResponse = api.sessionApi.routes(getSession)
        assertEquals(OK, getSessionResponse.status)

        val getSessionResponseBody = getBody<SessionDetailsOutputModel>(getSessionResponse.bodyString())
        assertNotNull(getSessionResponseBody)
    }

    @Test
    fun `gets sessions with wrong id and fails with 400`() {
        val wrongGameId = -1
        val getSession = Request(GET, "/session/search?gid=$wrongGameId")

        val getSessionResponse = api.sessionApi.routes(getSession)
        assertEquals(BAD_REQUEST, getSessionResponse.status)
    }

    @Test
    fun `gets sessions and succeeds with 200 and returns a list of Sessions`() {
        val getSessions = Request(GET, "/session/search?gid=$gameId")
        val getSessionsResponse = api.sessionApi.routes(getSessions)
        val getSessionsResponseBody = getBody<List<SessionDetailsOutputModel>>(getSessionsResponse.bodyString())

        assertEquals(OK, getSessionsResponse.status)
        getSessionsResponseBody.forEach {
            assertEquals(CAPACITY, it.capacity)
        }
    }

    @Test
    fun `gets sessions with invalid query values and fails with 400`() {
        val values = mapOf<String, Any>("gid" to -1, "pid" to -1, "state" to "abcd", "date" to "-0001-12-01")

        values.forEach {
            val getSessions = Request(GET, "/session/search?${it.key}=${it.value}")
            val getSessionsResponse = api.sessionApi.routes(getSessions)
            assertEquals(BAD_REQUEST, getSessionsResponse.status)
        }
    }

    @Test
    fun `removes a player from a session and succeeds with 200`() {
        val randomSession = sessionIds.random()

        val addPlayerToSession = addPlayerToSessionRequest(randomSession, guestId, guestToken)
        val addPlayerToSessionResponse = api.sessionApi.routes(addPlayerToSession)
        assertEquals(OK, addPlayerToSessionResponse.status)

        val removePlayer = removePlayerFromSessionRequest(randomSession, guestId, guestToken)
        val removePlayerResponse = api.sessionApi.routes(removePlayer)

        assertEquals(OK, removePlayerResponse.status)
    }

    @Test
    fun `deletes a session and succeeds with 200`() {
        val randomSession = sessionIds.removeLast()

        val deleteSession = deleteSessionRequest(randomSession, hostId, hostToken)
        val deleteSessionResponse = api.sessionApi.routes(deleteSession)

        assertEquals(OK, deleteSessionResponse.status)
    }

    @Test
    fun `tries to delete a session that doesnt exist and fails with 404`() {
        val deleteSession = deleteSessionRequest(99999999, hostId, hostToken)
        val deleteSessionResponse = api.sessionApi.routes(deleteSession)

        assertEquals(NOT_FOUND, deleteSessionResponse.status)
    }

    @Test
    fun `tries to delete a session with a wrong host ID and fails with 403`() {
        val randomSession = sessionIds.random()

        val deleteSession = deleteSessionRequest(randomSession, hostId + 10, hostToken)
        val deleteSessionResponse = api.sessionApi.routes(deleteSession)

        assertEquals(FORBIDDEN, deleteSessionResponse.status)
    }

    @Test
    fun `tries to update a session and succeeds with 200`() {
        val randomSession = sessionIds.random()
        val newDate: String = DATE_NOW.plusHours(2).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

        val updateSession = updateSessionRequest(randomSession, null, null, newDate, hostToken)
        val updateSessionResponse = api.sessionApi.routes(updateSession)

        assertEquals(OK, updateSessionResponse.status)
    }

    @Test
    fun `tries to update a session with wrong parameters and fails with 400`() {
        val randomSession = sessionIds.random()
        val newCapacity = -1

        val updateSession = updateSessionRequest(randomSession, newCapacity, null, null, hostToken)
        val updateSessionResponse = api.sessionApi.routes(updateSession)

        assertEquals(BAD_REQUEST, updateSessionResponse.status)
    }

    @Test
    fun `tries to update a session without the host token and fails with 401`() {
        val randomSession = sessionIds.random()
        val newCapacity = 40

        val updateSession = updateSessionRequest(randomSession, newCapacity, null, null, "123")
        val updateSessionResponse = api.sessionApi.routes(updateSession)

        assertEquals(UNAUTHORIZED, updateSessionResponse.status)
    }

    companion object {
        var gameId: Int
        var hostId: Int
        var hostToken: String
        var guestId: Int
        var guestToken: String

        private val sessionIds = mutableListOf<Int>()
        private val CAPACITY = 10
        private val DATE_NOW: OffsetDateTime = OffsetDateTime.now()
        private val DATE: String = DATE_NOW.plusHours(1).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

        init {
            clearDataBase()
            val hostData = createAndGetPlayerToken(api.playerApi)
            hostToken = hostData.token
            hostId = hostData.id
            val guestData = createAndGetPlayerToken(api.playerApi)
            guestToken = guestData.token
            guestId = guestData.id
            gameId = createBaseGame()
            createBaseSessions()
        }

        private fun createBaseSessions() {
            val amountOfSessions = 10
            repeat(amountOfSessions) {
                val createSession = createSessionRequest(
                    CAPACITY,
                    gameId,
                    DATE,
                    hostId,
                    hostToken
                )
                val createSessionResponse = api.sessionApi.routes(createSession)
                assertEquals(CREATED, createSessionResponse.status)
                val createSessionBody = getBody<SessionIdentifierOutputModel>(createSessionResponse.bodyString())
                sessionIds.add(createSessionBody.sessionId)
            }
        }

        private fun createBaseGame(): Int {
            val reqCreate = Request(POST, "/api/game")
                .header("authorization", "Bearer $hostToken")
                .header("content-type", "application/json")
                .body(
                    """
                    {
                        "name": "Horizon Zero Dawn",
                        "developer": "Guerrilla Games",
                        "genres": [
                            "RPG", "SHOOTER", "ADVENTURE","ACTION"
                        ]
                    } 
                """
                )

            val rsp1 = api.routes(reqCreate)
            return getBody<GameIdOutputModel>(rsp1.bodyString()).id
        }
    }
}