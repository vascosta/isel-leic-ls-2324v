package pt.isel.ls.integration

import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.junit.Assert.assertNotEquals
import org.junit.BeforeClass
import org.junit.Test
import pt.isel.ls.app.domain.ApiException
import pt.isel.ls.player.domain.CreatePlayerOutputModel
import pt.isel.ls.player.domain.GetPlayerHomeOutputModel
import pt.isel.ls.player.domain.GetPlayerInfoOutputModel
import pt.isel.ls.player.domain.LoginOutputModel
import pt.isel.ls.player.domain.SearchPlayersByNameOutputModel
import pt.isel.ls.utils.createAndGetPlayerToken
import pt.isel.ls.utils.InvalidBodyInputException
import pt.isel.ls.utils.InvalidEmailException
import pt.isel.ls.utils.InvalidPasswordException
import pt.isel.ls.utils.InvalidPlayerIdException
import pt.isel.ls.utils.InvalidPlayerNameException
import pt.isel.ls.utils.PlayerAlreadyExistsException
import pt.isel.ls.utils.PlayerNotFoundException
import pt.isel.ls.utils.PlayerNotLoggedInException
import pt.isel.ls.utils.getBody
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PlayerIntegrationTests: IntegrationBaseTest() {

    @Test
    fun `creates a player and then returns a response with status 201`() {
        // Arrange
        val randomName = "player" + (0..10000).random()
        val randomEmail = randomName + "@email.com"
        val request = Request(Method.POST, "/api/player")
            .header("content-type", "application/json")
            .body(
                """
                    {
                        "name": "$randomName",
                        "email": "$randomEmail",
                        "password": "Password1"
                    } 
                """
            )

        // Act
        val response = api.routes(request)
        val body = getBody<CreatePlayerOutputModel>(response.bodyString())

        // Assert
        assertEquals(Status.CREATED, response.status)
        assertNotNull(body)
    }

    @Test
    fun `try creating a player with invalid parameters and then returns a response with status 400`() {
        // Arrange
        val requestWithInvalidName = Request(Method.POST, "/api/player")
            .header("content-type", "application/json")
            .body(
                """
                    {
                        "name": "Manuel Da Silva Pereira Alberto",
                        "email": "manuel@email.com",
                        "password": "Password1"
                    } 
                """
            )

        val requestWithInvalidEmail = Request(Method.POST, "/api/player")
            .header("content-type", "application/json")
            .body(
                """
                    {
                        "name": "Manuel",
                        "email": "manuel@",
                        "password": "Password1"
                    } 
                """
            )

        val requestWithInvalidPassword = Request(Method.POST, "/api/player")
            .header("content-type", "application/json")
            .body(
                """
                    {
                        "name": "Manuel",
                        "email": "manuel@email.com",
                        "password": "pass"
                    } 
                """
            )

        val requestWithInvalidInput = Request(Method.POST, "/api/player")
            .header("content-type", "application/json")
            .body("{}")

        // Act
        val responseWithInvalidName = api.routes(requestWithInvalidName)
        val responseWithInvalidEmail = api.routes(requestWithInvalidEmail)
        val responseWithInvalidPassword = api.routes(requestWithInvalidPassword)
        val responseWithInvalidInput = api.routes(requestWithInvalidInput)
        val bodyWithInvalidName = getBody<ApiException>(responseWithInvalidName.bodyString())
        val bodyWithInvalidEmail = getBody<ApiException>(responseWithInvalidEmail.bodyString())
        val bodyWithInvalidPassword = getBody<ApiException>(responseWithInvalidPassword.bodyString())
        val bodyWithInvalidInput = getBody<ApiException>(responseWithInvalidInput.bodyString())

        // Assert
        assertEquals(BAD_REQUEST, responseWithInvalidName.status)
        assertEquals(InvalidPlayerNameException.message, bodyWithInvalidName.message)
        assertEquals(BAD_REQUEST, responseWithInvalidEmail.status)
        assertEquals(InvalidEmailException.message, bodyWithInvalidEmail.message)
        assertEquals(BAD_REQUEST, responseWithInvalidPassword.status)
        assertEquals(InvalidPasswordException.message, bodyWithInvalidPassword.message)
        assertEquals(BAD_REQUEST, responseWithInvalidInput.status)
        assertEquals(InvalidBodyInputException.message, bodyWithInvalidInput.message)
    }

    @Test
    fun `try creating a player that already exists and then returns a response with status code 400`() {
        // Arrange
        val randomName = "player" + (0..10000).random()
        val randomEmail = randomName + "@email.com"
        val request = Request(Method.POST, "/api/player")
            .header("content-type", "application/json")
            .body(
                """
                    {
                        "name": "$randomName",
                        "email": "$randomEmail",
                        "password": "Password1"
                    } 
                """
            )

        api.routes(request)

        // Act
        val response = api.routes(request)
        val body = getBody<ApiException>(response.bodyString())

        // Assert
        assertEquals(BAD_REQUEST, response.status)
        assertEquals(PlayerAlreadyExistsException.message, body.message)
    }

    @Test
    fun `login a player by name and password and then returns a response with status code 200`() {
        // Arrange
        val randomName = "player" + (0..10000).random()
        val randomEmail = randomName + "@email.com"
        val createRequest = Request(Method.POST, "/api/player")
            .header("content-type", "application/json")
            .body(
                """
                    {
                        "name": "$randomName",
                        "email": "$randomEmail",
                        "password": "Password1"
                    } 
                """
            )
        val createResponse = api.routes(createRequest)
        val createBody = getBody<CreatePlayerOutputModel>(createResponse.bodyString())

        val loginRequest = Request(Method.PATCH, "/api/login")
            .header("content-type", "application/json")
            .body(
                """
                    {
                        "name": "$randomName",
                        "password": "Password1"
                    } 
                """
            )

        // Act
        val loginResponse = api.routes(loginRequest)
        val loginBody = getBody<LoginOutputModel>(loginResponse.bodyString())

        // Assert
        assertEquals(Status.OK, loginResponse.status)
        assertNotNull(loginBody)
        assertEquals(createBody.id, loginBody.id)
        assertNotEquals(createBody.token, loginBody.token)
    }

    @Test
    fun `try login a player that does not exist by name and password and then returns a response with status code 404`() {
        // Arrange
        val randomName = "player" + (0..10000).random()
        val request = Request(Method.PATCH, "/api/login")
            .body(
                """
                    {
                        "name": "$randomName",
                        "password": "Password1"
                    } 
                """
            )

        // Act
        val response = api.routes(request)
        val body = getBody<ApiException>(response.bodyString())

        // Assert
        assertEquals(Status.NOT_FOUND, response.status)
        assertEquals(PlayerNotFoundException.message, body.message)
    }

    @Test
    fun `try login a player with invalid parameters and then returns a response with status code 400`() {
        // Arrange
        val requestWithInvalidName = Request(Method.PATCH, "/api/login")
            .header("content-type", "application/json")
            .body(
                """
                    {
                        "name": "Manuel Da Silva Pereira Alberto",
                        "password": "Password1"
                    } 
                """
            )

        val requestWithInvalidPassword = Request(Method.PATCH, "/api/login")
            .header("content-type", "application/json")
            .body(
                """
                    {
                        "name": "Manuel",
                        "password": "pass"
                    } 
                """
            )

        val requestWithInvalidInput = Request(Method.PATCH, "/api/login")
            .header("content-type", "application/json")
            .body("{}")

        // Act
        val responseWithInvalidName = api.routes(requestWithInvalidName)
        val responseWithInvalidPassword = api.routes(requestWithInvalidPassword)
        val responseWithInvalidInput = api.routes(requestWithInvalidInput)
        val bodyWithInvalidName = getBody<ApiException>(responseWithInvalidName.bodyString())
        val bodyWithInvalidPassword = getBody<ApiException>(responseWithInvalidPassword.bodyString())
        val bodyWithInvalidInput = getBody<ApiException>(responseWithInvalidInput.bodyString())

        // Assert
        assertEquals(BAD_REQUEST, responseWithInvalidName.status)
        assertEquals(InvalidPlayerNameException.message, bodyWithInvalidName.message)
        assertEquals(BAD_REQUEST, responseWithInvalidPassword.status)
        assertEquals(InvalidPasswordException.message, bodyWithInvalidPassword.message)
        assertEquals(BAD_REQUEST, responseWithInvalidInput.status)
        assertEquals(InvalidBodyInputException.message, bodyWithInvalidInput.message)
    }

    @Test
    fun `logout a player by token and then returns a response with status code 200`() {
        // Arrange
        val createRequest = Request(Method.POST, "/api/player")
            .header("content-type", "application/json")
            .body(
                """
                    {
                        "name": "Manuel",
                        "email": "manuel@email.com",
                        "password": "Password1"
                    } 
                """
            )
        val createResponse = api.routes(createRequest)
        val createBody = getBody<CreatePlayerOutputModel>(createResponse.bodyString())

        val loginRequest = Request(Method.PATCH, "/api/login")
            .header("content-type", "application/json")
            .body(
                """
                    {
                        "name": "Manuel",
                        "password": "Password1"
                    } 
                """
            )
        val loginResponse = api.routes(loginRequest)
        val loginBody = getBody<LoginOutputModel>(loginResponse.bodyString())

        val logoutRequestWithBearer = Request(Method.PATCH, "/api/logout")
            .header("Authorization", "Bearer ${loginBody.token}")

        // Act
        val logoutResponseWithBearer = api.routes(logoutRequestWithBearer)

        // Assert
        assertEquals(Status.OK, logoutResponseWithBearer.status)
        assertNotNull(loginBody)
        assertEquals(createBody.id, loginBody.id)
        assertNotEquals(createBody.token, loginBody.token)
    }

    @Test
    fun `try logout a player that does not exist by token and then returns a response with status code 401`() {
        // Arrange
        val request = Request(Method.PATCH, "/api/logout")
            .header("Authorization", "Bearer 1234567890")

        // Act
        val response = api.routes(request)
        val body = getBody<ApiException>(response.bodyString())

        // Assert
        assertEquals(Status.UNAUTHORIZED, response.status)
        assertEquals(PlayerNotLoggedInException.message, body.message)
    }

    @Test
    fun `gets a player home by token and then returns a response with status code 200`() {
        // Arrange
        val requestCreate = Request(Method.POST, "/api/player")
            .header("content-type", "application/json")
            .body(
                """
                    {
                        "name": "Joaquim",
                        "email": "joaquim@email.com",
                        "password": "Password1"
                    } 
                """
            )

        val responseCreate = api.routes(requestCreate)
        val playerInfo = getBody<CreatePlayerOutputModel>(responseCreate.bodyString())

        val requestGetPlayerHome = Request(Method.GET, "/api/home/${playerInfo.token}")

        // Act
        val responseGetPlayerHome = api.routes(requestGetPlayerHome)
        val bodyGetPlayerHome = getBody<GetPlayerHomeOutputModel>(responseGetPlayerHome.bodyString())

        // Assert
        assertEquals(Status.OK, responseGetPlayerHome.status)
        assertEquals(playerInfo.id, bodyGetPlayerHome.id)
        assertEquals("Joaquim", bodyGetPlayerHome.name)
    }

    @Test
    fun `try getting a player home that does not exist by token and then returns a response with status code 404`() {
        // Arrange
        val request = Request(Method.GET, "/api/home/1234567890")

        // Act
        val response = api.routes(request)
        val body = getBody<ApiException>(response.bodyString())

        // Assert
        assertEquals(Status.NOT_FOUND, response.status)
        assertEquals(PlayerNotFoundException.message, body.message)
    }

    @Test
    fun `gets a player info by id and then returns a response with status code 200`() {
        // Arrange
        val randomName = "player" + (0..10000).random()
        val randomEmail = randomName + "@email.com"
        val requestCreate = Request(Method.POST, "/api/player")
            .header("content-type", "application/json")
            .body(
                """
                    {
                        "name": "$randomName",
                        "email": "$randomEmail",
                        "password": "Password1"
                    } 
                """
            )

        val responseCreate = api.routes(requestCreate)
        val playerId = getBody<CreatePlayerOutputModel>(responseCreate.bodyString()).id

        val requestWithBearer = Request(Method.GET, "/api/player/$playerId")
            .header("Authorization", "Bearer $playerToken")

        // Act
        val responseWithBearer = api.routes(requestWithBearer)
        val bodyWithBearer = getBody<GetPlayerInfoOutputModel>(responseWithBearer.bodyString())

        // Assert
        assertEquals(Status.OK, responseWithBearer.status)
        assertEquals(playerId, bodyWithBearer.id)
        assertEquals(randomName, bodyWithBearer.name)
        assertEquals(randomEmail, bodyWithBearer.email)
    }

    @Test
    fun `try getting a player info with no Authorization by id and then returns a response with status code 401`() {
        // Arrange
        val randomName = "player" + (0..10000).random()
        val randomEmail = randomName + "@email.com"
        val requestCreate = Request(Method.POST, "/api/player")
            .header("content-type", "application/json")
            .body(
                """
                    {
                        "name": "$randomName",
                        "email": "$randomEmail",
                        "password": "Password1"
                    } 
                """
            )

        val responseCreate = api.routes(requestCreate)
        val playerId = getBody<CreatePlayerOutputModel>(responseCreate.bodyString()).id

        val request = Request(Method.GET, "/api/player/$playerId")

        // Act
        val response = api.routes(request)
        val body = getBody<ApiException>(response.bodyString())

        // Assert
        assertEquals(Status.UNAUTHORIZED, response.status)
        assertEquals(PlayerNotLoggedInException.message, body.message)
    }

    @Test
    fun `try getting a player info with an invalid id and then returns a response with status code 400`() {
        // Arrange
        val requestWithInvalidId = Request(Method.GET, "/api/player/-1")
            .header("Authorization", "Bearer $playerToken")
        val requestWithInvalidId2 = Request(Method.GET, "/api/player/manuel")
            .header("Authorization", "Bearer $playerToken")

        // Act
        val responseWithInvalidId = api.routes(requestWithInvalidId)
        val responseWithInvalidId2 = api.routes(requestWithInvalidId2)
        val bodyWithInvalidNumber = getBody<ApiException>(responseWithInvalidId.bodyString())
        val bodyWithInvalidId = getBody<ApiException>(responseWithInvalidId2.bodyString())

        // Assert
        assertEquals(BAD_REQUEST, responseWithInvalidId.status)
        assertEquals(InvalidPlayerIdException.message, bodyWithInvalidNumber.message)
        assertEquals(BAD_REQUEST, responseWithInvalidId2.status)
        assertEquals(InvalidPlayerIdException.message, bodyWithInvalidId.message)
    }

    @Test
    fun `try getting a player info that does not exist by id and then returns a response with status code 404`() {
        // Arrange
        val request = Request(Method.GET, "/api/player/1904")
            .header("Authorization", "Bearer $playerToken")

        // Act
        val response = api.routes(request)
        val body = getBody<ApiException>(response.bodyString())

        // Assert
        assertEquals(Status.NOT_FOUND, response.status)
        assertEquals(PlayerNotFoundException.message, body.message)
    }

    @Test
    fun `searches for players info by name and then returns a response with status 200`() {
        // Arrange
        val requestCreate = Request(Method.POST, "/api/player")
            .header("content-type", "application/json")
            .body(
                """
                    {
                        "name": "Joaquim",
                        "email": "joaquim@email.com",
                        "password": "Password1"
                    } 
                """
            )

        api.routes(requestCreate)
        val request = Request(Method.GET, "/api/players/search?name=joaquim")

        // Act
        val response = api.routes(request)
        val body = getBody<SearchPlayersByNameOutputModel>(response.bodyString())

        // Assert
        assertEquals(Status.OK, response.status)
        assertEquals(1, body.players.size)
        assertEquals("Joaquim", body.players.first().name)
        assertEquals("joaquim@email.com", body.players.first().email)
    }

    @Test
    fun `try searches for players info with an invalid name and then returns a response with status code 400`() {
        // Arrange
        val requestWithInvalidName = Request(Method.GET, "/api/players/search?name=joaquimdasilvapereiraalmeida")

        // Act
        val response = api.routes(requestWithInvalidName)
        val body = getBody<ApiException>(response.bodyString())

        // Assert
        assertEquals(BAD_REQUEST, response.status)
        assertEquals(InvalidPlayerNameException.message, body.message)
    }

    @Test
    fun `try searching for players info that does not exist by name and then returns a response with status code 200`() {
        // Arrange
        val randomName = "player" + (0..10000).random()
        val request = Request(Method.GET, "/api/players/search?name=manuel")

        // Act
        val response = api.routes(request)
        val body = getBody<SearchPlayersByNameOutputModel>(response.bodyString())

        // Assert
        assertEquals(Status.OK, response.status)
        assertTrue(body.players.isEmpty())
    }

    companion object {
        var playerToken: String

        init {
            clearDataBase()
            playerToken = createAndGetPlayerToken(api.playerApi).token
        }

    }
}