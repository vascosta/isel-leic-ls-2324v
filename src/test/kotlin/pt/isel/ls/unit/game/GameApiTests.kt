package pt.isel.ls.unit.game

import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.CREATED
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.UNAUTHORIZED
import org.junit.Test
import pt.isel.ls.app.domain.ApiException
import pt.isel.ls.auth.AuthHandler
import pt.isel.ls.game.api.GameApi
import pt.isel.ls.game.dataMem.GameDataMem
import pt.isel.ls.game.domain.GameIdOutputModel
import pt.isel.ls.game.domain.GameListOutputModel
import pt.isel.ls.game.domain.GameOutputModel
import pt.isel.ls.game.domain.Genres
import pt.isel.ls.game.services.GameServices
import pt.isel.ls.player.api.PlayerApi
import pt.isel.ls.player.dataMem.PlayerDataMem
import pt.isel.ls.player.services.PlayerServices
import pt.isel.ls.utils.createAndGetPlayerToken
import pt.isel.ls.utils.GameAlreadyExistsException
import pt.isel.ls.utils.GameNotFoundException
import pt.isel.ls.utils.InvalidBodyInputException
import pt.isel.ls.utils.InvalidDeveloperNameException
import pt.isel.ls.utils.InvalidDeveloperNameLengthException
import pt.isel.ls.utils.InvalidGameIdException
import pt.isel.ls.utils.InvalidGameNameException
import pt.isel.ls.utils.InvalidGameNameLengthException
import pt.isel.ls.utils.PlayerNotLoggedInException
import pt.isel.ls.utils.getBody
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GameApiTests {

    private val gameDataMem = GameDataMem()
    private val gameServices = GameServices(gameDataMem)
    private val playerServices = PlayerServices(PlayerDataMem())
    private val authHandler = AuthHandler(playerServices)
    private val api = GameApi(gameServices, authHandler)

    val playerToken = createAndGetPlayerToken(PlayerApi(playerServices, authHandler)).token

    @Test
    fun `creates a new game and returns a response with status 201`() {
        // Arrange
        val req = Request(Method.POST, "/game")
            .header("authorization", "Bearer $playerToken")
            .header("content-type", "application/json")
            .body(
                """
                    {
                        "name": "New Super Mario Bros",
                        "developer": "Nintendo",
                        "genres": [
                            "ACTION", "ADVENTURE"
                        ]
                    } 
                """
            )

        // Act
        val rsp = api.routes(req)
        val body = getBody<GameIdOutputModel>(rsp.bodyString())

        // Assert
        assertEquals(CREATED, rsp.status)
        assertNotNull(body)
        assertEquals(GameIdOutputModel(1), body)
        assertEquals(1, body.id)
    }

    @Test
    fun `tries to create a new game with invalid name and returns a response with status 400`() {
        // Arrange
        val req = Request(Method.POST, "/game")
            .header("authorization", "Bearer $playerToken")
            .header("content-type", "application/json")
            .body(
                """
                    {
                        "name": "New Super *Mario Bros",
                        "developer": "Nintendo",
                        "genres": [
                            "ACTION", "ADVENTURE"
                        ]
                    } 
                """
            )

        // Act
        val rsp = api.routes(req)
        val body = getBody<ApiException>(rsp.bodyString())

        // Assert
        assertEquals(BAD_REQUEST, rsp.status)
        assertEquals(InvalidGameNameException.message, body.message)
    }

    @Test
    fun `tries to create a new game with invalid developer and returns a response with status 400`() {
        // Arrange
        val req = Request(Method.POST, "/game")
            .header("authorization", "Bearer $playerToken")
            .header("content-type", "application/json")
            .body(
                """
                    {
                        "name": "Super Mario",
                        "developer": "Fake Nintendo Company with a huge name",
                        "genres": [
                            "ACTION", "ADVENTURE"
                        ]
                    } 
                """
            )

        // Act
        val rsp = api.routes(req)
        val body = getBody<ApiException>(rsp.bodyString())

        // Assert
        assertEquals(BAD_REQUEST, rsp.status)
        assertEquals(InvalidDeveloperNameLengthException.message, body.message)
    }

    @Test
    fun `tries to create a new game with invalid genres and returns a response with status 400`() {
        // Arrange
        val req = Request(Method.POST, "/game")
            .header("authorization", "Bearer $playerToken")
            .header("content-type", "application/json")
            .body(
                """
                    {
                        "name": "Mario Bros",
                        "developer": "Nintendo",
                        "genres": [
                            "ACTION", "ADVENTURE", "MYSTERY"
                        ]
                    } 
                """
            )

        // Act
        val rsp = api.routes(req)
        val body = getBody<ApiException>(rsp.bodyString())

        // Assert
        assertEquals(BAD_REQUEST, rsp.status)
        assertEquals(InvalidBodyInputException.message, body.message)
    }

    @Test
    fun `tries to create a game that already exists and returns a response with status 409`() {
        // Arrange
        val req1 = Request(Method.POST, "/game")
            .header("authorization", "Bearer $playerToken")
            .header("content-type", "application/json")
            .body(
                """
                    {
                        "name": "New Super Mario Bros",
                        "developer": "Nintendo",
                        "genres": [
                            "ACTION", "ADVENTURE"
                        ]
                    } 
                """
            )

        val req2 = Request(Method.POST, "/game")
            .header("authorization", "Bearer $playerToken")
            .header("content-type", "application/json")
            .body(
                """
                    {
                        "name": "New Super Mario Bros",
                        "developer": "Nintendo",
                        "genres": [
                            "ACTION"
                        ]
                    } 
                """
            )

        // Act
        val rsp = api.routes(req1)
        val rsp2 = api.routes(req2)
        val body2 = getBody<ApiException>(rsp2.bodyString())

        // Assert
        assertEquals(CREATED, rsp.status)
        assertEquals(BAD_REQUEST, rsp2.status)
        assertEquals(GameAlreadyExistsException.message, body2.message)
    }

    @Test
    fun `tries to create a game without authorization and returns a response with status 401`() {
        // Arrange
        val req = Request(Method.POST, "/game")
            .header("content-type", "application/json")
            .body(
                """
                    {
                        "name": "New Super Mario Bros",
                        "developer": "Nintendo",
                        "genres": [
                            "ACTION", "ADVENTURE"
                        ]
                    } 
                """
            )

        // Act
        val rsp = api.routes(req)
        val body = getBody<ApiException>(rsp.bodyString())

        // Assert
        assertEquals(UNAUTHORIZED, rsp.status)
        assertEquals(PlayerNotLoggedInException.message, body.message)
    }

    @Test
    fun `gets the details of game according to its id and returns a response with status 200`() {
        // Arrange
        val name = "Call Of Duty Modern Warfare"
        val developer = "Infinity Ward"
        val genres = listOf(Genres.SHOOTER, Genres.ACTION)
        val reqCreate = Request(Method.POST, "/game")
            .header("authorization", "Bearer $playerToken")
            .header("content-type", "application/json")
            .body(
                """
                    {
                        "name": "Call Of Duty Modern Warfare",
                        "developer": "Infinity Ward",
                        "genres": [
                            "SHOOTER", "ACTION"
                        ]
                    } 
                """
            )
        val rspCreate = api.routes(reqCreate)
        val rspBody = getBody<GameIdOutputModel>(rspCreate.bodyString()).id

        val req = Request(Method.GET, "/game/$rspBody")

        // Act
        val rsp = api.routes(req)
        val body = getBody<GameOutputModel>(rsp.bodyString())

        // Assert
        assertEquals(OK, rsp.status)
        assertNotNull(body)
        assertEquals(rspBody, body.id)
        assertEquals(name, body.name)
        assertEquals(developer, body.developer)
        assertEquals(genres, body.genres)
    }

    @Test
    fun `tries to get details of a game with id mismatch and returns a response with status 400`() {
        // Arrange
        val invalidId = "A"
        val req = Request(Method.GET, "/game/$invalidId")

        // Act
        val rsp = api.routes(req)
        val body = getBody<ApiException>(rsp.bodyString())

        // Assert
        assertEquals(BAD_REQUEST, rsp.status)
        assertFailsWith<Exception> {
            getBody<GameOutputModel>(rsp.bodyString())
        }
        assertEquals(InvalidGameIdException.message, body.message)
    }

    @Test
    fun `tries to get details of a game that doesn't exist and returns a response with status 404`() {
        // Arrange
        val invalidId = 99999
        val req = Request(Method.GET, "/game/$invalidId")

        // Act
        val rsp = api.routes(req)
        val body = getBody<ApiException>(rsp.bodyString())

        // Assert
        assertEquals(NOT_FOUND, rsp.status)
        assertFailsWith<Exception> {
            getBody<GameOutputModel>(rsp.bodyString())
        }
        assertEquals(GameNotFoundException.message, body.message)
    }

    @Test
    fun `tries to get details of a game with invalid id and returns a response with status 400`() {
        // Arrange
        val invalidId = - 1
        val req = Request(Method.GET, "/game/$invalidId")

        // Act
        val rsp = api.routes(req)
        val body = getBody<ApiException>(rsp.bodyString())

        // Assert
        assertEquals(BAD_REQUEST, rsp.status)
        assertFailsWith<Exception> {
            getBody<GameOutputModel>(rsp.bodyString())
        }
        assertEquals(InvalidGameIdException.message, body.message)
    }

    @Test
    fun `gets a list of games according to its genre and returns a response with 200`() {
        // Arrange
        val name = "Horizon Zero Dawn"
        val developer = "Guerrilla Games"
        val genres = listOf(Genres.RPG, Genres.SHOOTER, Genres.ADVENTURE, Genres.ACTION)
        val reqCreate = Request(Method.POST, "/game")
            .header("authorization", "Bearer $playerToken")
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
        val id1 = getBody<GameIdOutputModel>(rsp1.bodyString())

        val name2 = "Horizon Forbidden West"
        val reqCreate2 = Request(Method.POST, "/game")
            .header("authorization", "Bearer $playerToken")
            .header("content-type", "application/json")
            .body(
                """
                    {
                        "name": "Horizon Forbidden West",
                        "developer": "Guerrilla Games",
                        "genres": [
                            "RPG", "SHOOTER", "ADVENTURE","ACTION"
                        ]
                    } 
                """
            )

        val rsp2 = api.routes(reqCreate2)
        val id2 = getBody<GameIdOutputModel>(rsp2.bodyString())

        val req = Request(Method.GET, "/games?genres=rpg")

        // Act
        val games = api.routes(req)
        val rsp = getBody<GameListOutputModel>(games.bodyString())

        // Assert
        assertEquals(OK, games.status)
        assertNotNull(rsp.games)
        assertEquals(2, rsp.games.size)
        assertEquals(
            listOf(
                GameOutputModel(id1.id, name, developer, genres),
                GameOutputModel(id2.id, name2, developer, genres)
            ),
            rsp.games
        )
    }

    @Test
    fun `gets a list of games according to its developer and returns a response with 200`() {
        // Arrange
        val name = "Horizon Zero"
        val developer = "Games2"
        val genres = listOf(Genres.RPG, Genres.SHOOTER, Genres.ADVENTURE, Genres.ACTION)
        val reqCreate = Request(Method.POST, "/game")
            .header("authorization", "Bearer $playerToken")
            .header("content-type", "application/json")
            .body(
                """
                    {
                        "name": "Horizon Zero",
                        "developer": "Games2",
                        "genres": [
                            "RPG", "SHOOTER", "ADVENTURE","ACTION"
                        ]
                    } 
                """
            )

        val rsp1 = api.routes(reqCreate)
        val id1 = getBody<GameIdOutputModel>(rsp1.bodyString())

        val name2 = "Horizon Forbidden"
        val reqCreate2 = Request(Method.POST, "/game")
            .header("authorization", "Bearer $playerToken")
            .header("content-type", "application/json")
            .body(
                """
                    {
                        "name": "Horizon Forbidden",
                        "developer": "Games2",
                        "genres": [
                            "RPG", "SHOOTER", "ADVENTURE","ACTION"
                        ]
                    } 
                """
            )

        val rsp2 = api.routes(reqCreate2)
        val id2 = getBody<GameIdOutputModel>(rsp2.bodyString())

        val req = Request(Method.GET, "/games?developer=Games2")

        // Act
        val games = api.routes(req)
        val rsp = getBody<GameListOutputModel>(games.bodyString())

        // Assert
        assertEquals(OK, games.status)
        assertNotNull(rsp.games)
        assertEquals(2, rsp.games.size)
        assertEquals(
            listOf(
                GameOutputModel(id1.id, name, developer, genres),
                GameOutputModel(id2.id, name2, developer, genres)
            ),
            rsp.games
        )
    }

    @Test
    fun `gets a list of games according to its genre and developer and returns a response with 200`() {
        // Arrange
        val name = "Horizon"
        val developer = "Guerrilla"
        val genres = listOf(Genres.RPG, Genres.SHOOTER, Genres.ADVENTURE, Genres.ACTION)
        val reqCreate = Request(Method.POST, "/game")
            .header("authorization", "Bearer $playerToken")
            .header("content-type", "application/json")
            .body(
                """
                    {
                        "name": "Horizon",
                        "developer": "Guerrilla",
                        "genres": [
                            "RPG", "SHOOTER", "ADVENTURE","ACTION"
                        ]
                    } 
                """
            )

        val rsp1 = api.routes(reqCreate)
        val id1 = getBody<GameIdOutputModel>(rsp1.bodyString())

        val name2 = "Forbidden West"
        val reqCreate2 = Request(Method.POST, "/game")
            .header("authorization", "Bearer $playerToken")
            .header("content-type", "application/json")
            .body(
                """
                    {
                        "name": "Forbidden West",
                        "developer": "Guerrilla",
                        "genres": [
                            "RPG", "SHOOTER", "ADVENTURE","ACTION"
                        ]
                    } 
                """
            )

        val rsp2 = api.routes(reqCreate2)
        val id2 = getBody<GameIdOutputModel>(rsp2.bodyString())

        val req = Request(Method.GET, "/games?genres=rpg,shooter,adventure,action&developer=Guerrilla")

        // Act
        val games = api.routes(req)
        val rsp = getBody<GameListOutputModel>(games.bodyString())

        // Assert
        assertEquals(OK, games.status)
        assertNotNull(rsp.games)
        assertEquals(2, rsp.games.size)
        assertEquals(
            listOf(
                GameOutputModel(id1.id, name, developer, genres),
                GameOutputModel(id2.id, name2, developer, genres)
            ),
            rsp.games
        )
    }

    @Test
    fun `gets an empty list of games according to its genre and developer and returns a response with 200`() {
        // Arrange
        val req = Request(Method.GET, "/games?genres=rpg,shooter&developer=Guerrilla-Games")

        // Act
        val games = api.routes(req)
        val rsp = getBody<GameListOutputModel>(games.bodyString())

        // Assert
        assertEquals(OK, games.status)
        assertTrue(rsp.games.isEmpty())
    }

    @Test
    fun `tries to get a list of games according to its genre and invalid developer and returns a response with 400`() {
        // Arrange
        val req = Request(Method.GET, "/games?genres=rpg,shooter&developer=Guerrilla-Games-Productions-Entertainment-Enterprises")

        // Act
        val rsp = api.routes(req)
        val body = getBody<ApiException>(rsp.bodyString())

        // Assert
        assertEquals(BAD_REQUEST, rsp.status)
        assertFailsWith<Exception> {
            getBody<GameListOutputModel>(rsp.bodyString())
        }
        assertEquals(InvalidDeveloperNameLengthException.message, body.message)
    }

    @Test
    fun `gets a game by its name and returns a response with 200`() {
        // Arrange
        val name = "The Witcher 3"
        val req1 = Request(Method.POST, "/game")
            .header("authorization", "Bearer $playerToken")
            .header("content-type", "application/json")
            .body(
                """
                    {
                        "name": "The Witcher 3",
                        "developer": "CD Projekt",
                        "genres": [
                            "RPG", "ACTION", "ADVENTURE"
                        ]
                    }
                """
            )

        val rsp1 = api.routes(req1)
        val id = getBody<GameIdOutputModel>(rsp1.bodyString()).id

        val req2 = Request(Method.GET, "/game/$id")
        val rsp2 = api.routes(req2)
        val game = getBody<GameOutputModel>(rsp2.bodyString())

        val req = Request(Method.GET, "/games/?name=${name.replace(" ", "-")}")

        // Act
        val gm = api.routes(req)
        val rsp = getBody<GameListOutputModel>(gm.bodyString())

        // Assert
        assertEquals(OK, gm.status)
        assertEquals(1, rsp.games.size)
        assertEquals(
            listOf(
                GameOutputModel(game.id, game.name, game.developer, game.genres)
            ),
            rsp.games
        )
    }

    @Test
    fun `tries to get a list of games by its invalid name and returns a response with 400`() {
        // Arrange
        val req = Request(Method.GET, "/games?name=Game-With-Big-Big-Big-Big-Big-Big-Big-Big-Big-Big-Big-Big-Big-Name")

        // Act
        val rsp = api.routes(req)
        val body = getBody<ApiException>(rsp.bodyString())

        // Assert
        assertEquals(BAD_REQUEST, rsp.status)
        assertFailsWith<Exception> {
            getBody<GameListOutputModel>(rsp.bodyString())
        }
        assertEquals(InvalidGameNameLengthException.message, body.message)
    }

    @Test
    fun `gets game by its name with no capital letters and returns a response with 200`() {
        // Arrange
        val name = "The Witcher 4"
        val req1 = Request(Method.POST, "/game")
            .header("authorization", "Bearer $playerToken")
            .header("content-type", "application/json")
            .body(
                """
                    {
                        "name": "The Witcher 4",
                        "developer": "CD Projekt",
                        "genres": [
                            "RPG", "ACTION", "ADVENTURE"
                        ]
                    }
                """
            )

        val rsp1 = api.routes(req1)
        val id = getBody<GameIdOutputModel>(rsp1.bodyString()).id

        val req2 = Request(Method.GET, "/game/$id")
        val rsp2 = api.routes(req2)
        val game = getBody<GameOutputModel>(rsp2.bodyString())

        val nameSearched = name.lowercase()
        val req = Request(Method.GET, "/games/?name=${nameSearched.replace(" ", "-")}")

        // Act
        val gm = api.routes(req)
        val rsp = getBody<GameListOutputModel>(gm.bodyString())

        // Assert
        assertEquals(OK, gm.status)
        assertEquals(1, rsp.games.size)
        assertEquals(
            listOf(
                GameOutputModel(game.id, game.name, game.developer, game.genres)
            ),
            rsp.games
        )
    }

    @Test
    fun `gets two games by its partial name and returns its details`() {
        // Arrange
        val req1 = Request(Method.POST, "/game")
            .header("authorization", "Bearer $playerToken")
            .header("content-type", "application/json")
            .body(
                """
                    {
                        "name": "Resident Evil 4",
                        "developer": "Capcom",
                        "genres": [
                            "HORROR"
                        ]
                    }
                """
            )

        val rsp1 = api.routes(req1)
        val id1 = getBody<GameIdOutputModel>(rsp1.bodyString()).id

        val req2 = Request(Method.POST, "/game")
            .header("authorization", "Bearer $playerToken")
            .header("content-type", "application/json")
            .body(
                """
                    {
                        "name": "Resident Evil 2",
                        "developer": "Capcom",
                        "genres": [
                            "HORROR"
                        ]
                    }
                """
            )

        val rsp2 = api.routes(req2)
        val id2 = getBody<GameIdOutputModel>(rsp2.bodyString()).id

        val req3 = Request(Method.GET, "/game/$id1")
        val rsp3 = api.routes(req3)
        val game1 = getBody<GameOutputModel>(rsp3.bodyString())

        val req4 = Request(Method.GET, "/game/$id2")
        val rsp4 = api.routes(req4)
        val game2 = getBody<GameOutputModel>(rsp4.bodyString())

        val searchedName = "resident"
        val req = Request(Method.GET, "/games/?name=$searchedName")

        // Act
        val gm = api.routes(req)
        val rsp = getBody<GameListOutputModel>(gm.bodyString())

        // Assert
        assertEquals(2, rsp.games.size)
        assertEquals(
            listOf(
                GameOutputModel(game1.id, game1.name, game1.developer, game1.genres),
                GameOutputModel(game2.id, game2.name, game2.developer, game2.genres)
            ),
            rsp.games
        )
    }

    @Test
    fun `tries to get non existing game by name and returns a response with 200`() {
        // Arrange
        val fakeGame = "Uno"

        val req = Request(Method.GET, "/games/?name=$fakeGame")

        // Act
        val gm = api.routes(req)
        val rsp = getBody<GameListOutputModel>(gm.bodyString())

        // Assert
        assertEquals(OK, gm.status)
        assertEquals(emptyList(), rsp.games)
    }

    @Test
    fun `gets a list of games according to its name and genre and returns a response with 200`() {
        // Arrange
        val name = "Horizon3"
        val developer = "Guerrilla Games"
        val genres = listOf(Genres.RPG, Genres.SHOOTER, Genres.ADVENTURE, Genres.ACTION)
        val reqCreate = Request(Method.POST, "/game")
            .header("authorization", "Bearer $playerToken")
            .header("content-type", "application/json")
            .body(
                """
                    {
                        "name": "Horizon3",
                        "developer": "Guerrilla Games",
                        "genres": [
                            "RPG", "SHOOTER", "ADVENTURE","ACTION"
                        ]
                    } 
                """
            )

        val rsp1 = api.routes(reqCreate)
        val id1 = getBody<GameIdOutputModel>(rsp1.bodyString())

        val name2 = "Horizon4"
        val reqCreate2 = Request(Method.POST, "/game")
            .header("authorization", "Bearer $playerToken")
            .header("content-type", "application/json")
            .body(
                """
                    {
                        "name": "Horizon4",
                        "developer": "Guerrilla Games",
                        "genres": [
                            "RPG", "SHOOTER", "ADVENTURE","ACTION"
                        ]
                    } 
                """
            )

        val rsp2 = api.routes(reqCreate2)
        val id2 = getBody<GameIdOutputModel>(rsp2.bodyString())

        val req = Request(Method.GET, "/games?name=Horizon3&genres=action")

        // Act
        val games = api.routes(req)
        val rsp = getBody<GameListOutputModel>(games.bodyString())

        // Assert
        assertEquals(OK, games.status)
        assertNotNull(rsp.games)
        assertEquals(1, rsp.games.size)
        assertEquals(
            listOf(
                GameOutputModel(id1.id, name, developer, genres),
            ),
            rsp.games
        )
    }

    @Test
    fun `gets a list of games according to its name and developer and returns a response with 200`() {
        // Arrange
        val name = "HZD"
        val developer = "Guerrilla Games"
        val genres = listOf(Genres.RPG, Genres.SHOOTER, Genres.ADVENTURE, Genres.ACTION)
        val reqCreate = Request(Method.POST, "/game")
            .header("authorization", "Bearer $playerToken")
            .header("content-type", "application/json")
            .body(
                """
                    {
                        "name": "HZD",
                        "developer": "Guerrilla Games",
                        "genres": [
                            "RPG", "SHOOTER", "ADVENTURE","ACTION"
                        ]
                    } 
                """
            )

        val rsp1 = api.routes(reqCreate)
        val id1 = getBody<GameIdOutputModel>(rsp1.bodyString())

        val name2 = "HFW"
        val reqCreate2 = Request(Method.POST, "/game")
            .header("authorization", "Bearer $playerToken")
            .header("content-type", "application/json")
            .body(
                """
                    {
                        "name": "HFW",
                        "developer": "Guerrilla Games",
                        "genres": [
                            "RPG", "SHOOTER", "ADVENTURE","ACTION"
                        ]
                    } 
                """
            )

        val rsp2 = api.routes(reqCreate2)
        val id2 = getBody<GameIdOutputModel>(rsp2.bodyString())


        val req = Request(Method.GET, "/games?name=HFW&developer=Guerrilla-Games")

        // Act
        val games = api.routes(req)
        val rsp = getBody<GameListOutputModel>(games.bodyString())

        // Assert
        assertEquals(OK, games.status)
        assertNotNull(rsp.games)
        assertEquals(1, rsp.games.size)
        assertEquals(
            listOf(
                GameOutputModel(id2.id, name2, developer, genres),
            ),
            rsp.games
        )
    }

    @Test
    fun `gets a list of games according to its name, with invalid format and returns a response with 400`() {
        // Arrange
        val req = Request(Method.GET, "/games?name=Horizon Forbidden West")

        // Act
        val rsp = api.routes(req)
        val body = getBody<ApiException>(rsp.bodyString())

        // Assert
        assertEquals(BAD_REQUEST, rsp.status)
        assertFailsWith<Exception> {
            getBody<GameListOutputModel>(rsp.bodyString())
        }
        assertEquals(InvalidGameNameException.message, body.message)
    }

    @Test
    fun `gets a list of games according to its developer, with invalid format and returns a response with 400`() {
        // Arrange
        val req = Request(Method.GET, "/games?developer=Guerrilla Games")

        // Act
        val rsp = api.routes(req)
        val body = getBody<ApiException>(rsp.bodyString())

        // Assert
        assertEquals(BAD_REQUEST, rsp.status)
        assertFailsWith<Exception> {
            getBody<GameListOutputModel>(rsp.bodyString())
        }
        assertEquals(InvalidDeveloperNameException.message, body.message)
    }
}
