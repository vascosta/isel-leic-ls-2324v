package pt.isel.ls.unit.session

import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.CREATED
import org.http4k.core.Status.Companion.FORBIDDEN
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.UNAUTHORIZED
import org.junit.Before
import pt.isel.ls.auth.AuthHandler
import pt.isel.ls.player.api.PlayerApi
import pt.isel.ls.player.dataMem.PlayerDataMem
import pt.isel.ls.player.services.PlayerServices
import pt.isel.ls.session.api.SessionApi
import pt.isel.ls.session.domain.Session
import pt.isel.ls.session.domain.models.output.SessionDetailsOutputModel
import pt.isel.ls.session.domain.models.output.SessionIdentifierOutputModel
import pt.isel.ls.session.services.SessionServices
import pt.isel.ls.utils.addPlayerToSessionRequest
import pt.isel.ls.utils.createAndGetPlayerToken
import pt.isel.ls.utils.createSessionRequest
import pt.isel.ls.utils.deleteSessionRequest
import pt.isel.ls.utils.getBody
import pt.isel.ls.utils.removePlayerFromSessionRequest
import pt.isel.ls.utils.updateSessionRequest
import java.time.OffsetDateTime
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SessionApiTests : SessionBaseTest() {

    private val sessionServices = SessionServices(sessionDataMem)
    private val playerServices = PlayerServices(PlayerDataMem())
    private val authHandler = AuthHandler(playerServices)
    private val api = SessionApi(sessionServices, authHandler)

    private val playerApi = PlayerApi(playerServices, authHandler)
    private val playerToken = createAndGetPlayerToken(playerApi).token

    @Before
    fun `session template`() {
        val amountOfSessions = 30
        repeat(amountOfSessions) {
            val createSession = createSessionRequest(
                Random.nextInt(2, CAPACITY),
                Random.nextInt(1, GAMED),
                DATE,
                HOST,
                playerToken
            )
            val createSessionResponse = api.routes(createSession)
            assertEquals(CREATED, createSessionResponse.status)
            val createSessionBody = getBody<SessionIdentifierOutputModel>(createSessionResponse.bodyString())
            sessionIds.add(createSessionBody.sessionId)
        }
    }

    @Test
    fun `creates session and succeeds with 201`() {
        val createSession = createSessionRequest(CAPACITY, GAMED, DATE, HOST, playerToken)
        val createSessionResponse = api.routes(createSession)
        val body = getBody<SessionIdentifierOutputModel>(createSessionResponse.bodyString())

        assertEquals(CREATED, createSessionResponse.status)
        assertNotNull(body)
    }

    @Test
    fun `adds a player to a session and succeeds with 200`() {
        println(sessionIds)
        val addPlayer = addPlayerToSessionRequest(sessionIds.random(), PLAYER, playerToken)
        val addPlayerResponse = api.routes(addPlayer)

        assertEquals(OK, addPlayerResponse.status)
    }

    @Test
    fun `adds a player to a session that doesnt exist and fails with 404`() {
        val addPlayer = addPlayerToSessionRequest(sessionIds.size + 1, PLAYER, playerToken)
        val addPlayerResponse = api.routes(addPlayer)

        assertEquals(NOT_FOUND, addPlayerResponse.status)
    }

    @Test
    fun `gets session info and succeeds with 200 and returns the Session`() {
        val randomSession = sessionIds.random()
        val getSession = Request(GET, "/session/info/$randomSession")
        val getSessionResponse = api.routes(getSession)
        assertEquals(OK, getSessionResponse.status)

        val getSessionResponseBody = getBody<SessionDetailsOutputModel>(getSessionResponse.bodyString())
        assertNotNull(getSessionResponseBody)
    }

    @Test
    fun `gets sessions with wrong id and fails with 400`() {
        val wrongGameId = -1
        val getSession = Request(GET, "/session/search?gid=$wrongGameId")

        val getSessionResponse = api.routes(getSession)
        assertEquals(BAD_REQUEST, getSessionResponse.status)
    }

    @Test
    fun `gets sessions and succeeds with 200 and returns a list of Sessions`() {
        val getSessions = Request(GET, "/session/search?gid=$GAMED")
        val getSessionsResponse = api.routes(getSessions)
        val getSessionsResponseBody = getBody<List<Session>>(getSessionsResponse.bodyString())

        assertEquals(OK, getSessionsResponse.status)
        getSessionsResponseBody.forEachIndexed { index, session ->
            assertEquals(CAPACITY + index, session.capacity)
        }
    }

    @Test
    fun `gets sessions with invalid query values and fails with 400`() {
        val values = mapOf<String, Any>("gid" to -1, "pid" to -1, "state" to "abcd", "date" to "-0001-12-01")

        values.forEach {
            val getSessions = Request(GET, "/session/search?${it.key}=${it.value}")
            val getSessionsResponse = api.routes(getSessions)
            assertEquals(BAD_REQUEST, getSessionsResponse.status)
        }
    }

    @Test
    fun `removes a player from a session and succeeds with 200`() {
        val randomSession = sessionIds.random()

        val addPlayerToSession = addPlayerToSessionRequest(randomSession, GUEST, playerToken)
        val addPlayerToSessionResponse = api.routes(addPlayerToSession)
        assertEquals(OK, addPlayerToSessionResponse.status)

        val removePlayer = removePlayerFromSessionRequest(randomSession, GUEST, playerToken)
        val removePlayerResponse = api.routes(removePlayer)

        assertEquals(OK, removePlayerResponse.status)
    }

    @Test
    fun `deletes a session and succeeds with 200`() {
        val randomSession = sessionIds.random()

        println(sessionIds)

        val deleteSession = deleteSessionRequest(randomSession, HOST, playerToken)
        val deleteSessionResponse = api.routes(deleteSession)

        assertEquals(OK, deleteSessionResponse.status)
    }

    @Test
    fun `tries to delete a session that doesnt exist and fails with 404`() {
        val deleteSession = deleteSessionRequest(sessionIds.size + 2, HOST, playerToken)
        val deleteSessionResponse = api.routes(deleteSession)

        assertEquals(NOT_FOUND, deleteSessionResponse.status)
    }

    @Test
    fun `tries to delete a session with a wrong host ID and fails with 403`() {
        val randomSession = sessionIds.random()

        val deleteSession = deleteSessionRequest(randomSession, HOST + 1, playerToken)
        val deleteSessionResponse = api.routes(deleteSession)

        assertEquals(FORBIDDEN, deleteSessionResponse.status)
    }

    @Test
    fun `tries to update a session and succeeds with 200`() {
        val randomSession = sessionIds.random()
        val newCapacity = 40

        val updateSession = updateSessionRequest(randomSession, newCapacity, null, null, playerToken)
        val updateSessionResponse = api.routes(updateSession)

        assertEquals(OK, updateSessionResponse.status)
    }

    @Test
    fun `tries to update a session with wrong parameters and fails with 400`() {
        val randomSession = sessionIds.random()
        val newCapacity = -1

        val updateSession = updateSessionRequest(randomSession, newCapacity, null, null, playerToken)
        val updateSessionResponse = api.routes(updateSession)

        assertEquals(BAD_REQUEST, updateSessionResponse.status)
    }

    @Test
    fun `tries to update a session without the host token and fails with 401`() {
        val randomSession = sessionIds.random()
        val newCapacity = 40

        val updateSession = updateSessionRequest(randomSession, newCapacity, null, null, "123")
        val updateSessionResponse = api.routes(updateSession)

        assertEquals(UNAUTHORIZED, updateSessionResponse.status)
    }
}
