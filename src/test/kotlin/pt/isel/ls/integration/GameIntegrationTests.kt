package pt.isel.ls.integration

import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.junit.BeforeClass
import org.junit.Test
import pt.isel.ls.app.domain.ApiException
import pt.isel.ls.game.domain.GameIdOutputModel
import pt.isel.ls.game.domain.GameListOutputModel
import pt.isel.ls.game.domain.GameOutputModel
import pt.isel.ls.game.domain.Genres
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

class GameIntegrationTests: IntegrationBaseTest() {

    @Test
    fun `creates a new game and returns a response with status 201`() {
        // Arrange
        val req = Request(Method.POST, "/api/game")
            .header("authorization", "Bearer $playerToken")
            .header("content-type", "application/json")
            .body(
                """
                    {
                        "name": "New Super Mario Bros and Sisters",
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
        assertEquals(Status.CREATED, rsp.status)
        assertNotNull(body)
    }

    @Test
    fun `tries to create a new game with invalid name and returns a response with status 400`() {
        // Arrange
        val req = Request(Method.POST, "/api/game")
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
        assertEquals(Status.BAD_REQUEST, rsp.status)
        assertEquals(InvalidGameNameException.message, body.message)
    }

    @Test
    fun `tries to create a new game with invalid developer and returns a response with status 400`() {
        // Arrange
        val req = Request(Method.POST, "/api/game")
            .header("authorization", "Bearer $playerToken")
            .header("content-type", "application/json")
            .body(
                """
                    {
                        "name": "New Super Mario Bros",
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
        assertEquals(Status.BAD_REQUEST, rsp.status)
        assertEquals(InvalidDeveloperNameLengthException.message, body.message)
    }

    @Test
    fun `tries to create a new game with invalid genres and returns a response with status 400`() {
        // Arrange
        val req = Request(Method.POST, "/api/game")
            .header("authorization", "Bearer $playerToken")
            .header("content-type", "application/json")
            .body(
                """
                    {
                        "name": "New Super Mario Bros",
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
        assertEquals(Status.BAD_REQUEST, rsp.status)
        assertEquals(InvalidBodyInputException.message, body.message)
    }

    @Test
    fun `tries to create a game that already exists and returns a response with status 409`() {
        // Arrange
        val req = Request(Method.POST, "/api/game")
            .header("authorization", "Bearer $playerToken")
            .header("content-type", "application/json")
            .body(
                """
                    {
                        "name": "Horizon Zero Dawn",
                        "developer": "Nintendo",
                        "genres": [
                            "ACTION"
                        ]
                    } 
                """
            )

        // Act
        val rsp = api.routes(req)
        val body = getBody<ApiException>(rsp.bodyString())

        // Assert
        assertEquals(Status.BAD_REQUEST, rsp.status)
        assertEquals(GameAlreadyExistsException.message, body.message)
    }

    @Test
    fun `tries to create a game without authorization and returns a response with status 401`() {
        // Arrange
        val req = Request(Method.POST, "/api/game")
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
        assertEquals(Status.UNAUTHORIZED, rsp.status)
        assertEquals(PlayerNotLoggedInException.message, body.message)
    }

    @Test
    fun `gets the details of game according to its id and returns a response with status 200`() {
        // Arrange
        val name = "Call Of Duty Modern Warfare"
        val developer = "Infinity Ward"
        val genres = listOf(Genres.SHOOTER, Genres.ACTION)
        val reqCreate = Request(Method.POST, "/api/game")
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

        val req = Request(Method.GET, "/api/game/$rspBody")

        // Act
        val rsp = api.routes(req)
        val body = getBody<GameOutputModel>(rsp.bodyString())

        // Assert
        assertEquals(Status.OK, rsp.status)
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
        val req = Request(Method.GET, "/api/game/$invalidId")

        // Act
        val rsp = api.routes(req)
        val body = getBody<ApiException>(rsp.bodyString())

        // Assert
        assertEquals(Status.BAD_REQUEST, rsp.status)
        assertFailsWith<Exception> {
            getBody<GameOutputModel>(rsp.bodyString())
        }
        assertEquals(InvalidGameIdException.message, body.message)
    }

    @Test
    fun `tries to get details of a game that doesn't exist and returns a response with status 404`() {
        // Arrange
        val invalidId = 999999999
        val req = Request(Method.GET, "/api/game/$invalidId")

        // Act
        val rsp = api.routes(req)
        val body = getBody<ApiException>(rsp.bodyString())

        // Assert
        assertEquals(Status.NOT_FOUND, rsp.status)
        assertFailsWith<Exception> {
            getBody<GameOutputModel>(rsp.bodyString())
        }
        assertEquals(GameNotFoundException.message, body.message)
    }

    @Test
    fun `tries to get details of a game with invalid id and returns a response with status 400`() {
        // Arrange
        val invalidId = - 1
        val req = Request(Method.GET, "/api/game/$invalidId")

        // Act
        val rsp = api.routes(req)
        val body = getBody<ApiException>(rsp.bodyString())

        // Assert
        assertEquals(Status.BAD_REQUEST, rsp.status)
        assertFailsWith<Exception> {
            getBody<GameOutputModel>(rsp.bodyString())
        }
        assertEquals(InvalidGameIdException.message, body.message)
    }

    @Test
    fun `gets a list of games according to its genre and returns a response with 200`() {
        // Arrange
        val name = "Horizon Zero Dawn"
        val name2 = "Horizon Forbidden West"
        val developer = "Guerrilla Games"
        val genres = listOf(Genres.RPG, Genres.SHOOTER, Genres.ADVENTURE, Genres.ACTION)

        val req = Request(Method.GET, "/api/games?genres=rpg")

        // Act
        val games = api.routes(req)
        val rsp = getBody<GameListOutputModel>(games.bodyString())

        // Assert
        assertEquals(Status.OK, games.status)
        assertNotNull(rsp.games)
        assertEquals(2, rsp.games.size)
        assertEquals(
            listOf(
                GameOutputModel(idArgs.first(), name, developer, genres),
                GameOutputModel(idArgs.last(), name2, developer, genres)
            ),
            rsp.games
        )
    }

    @Test
    fun `gets a list of games according to its developer and returns a response with 200`() {
        // Arrange
        val name = "Horizon Zero Dawn"
        val developer = "Guerrilla Games"
        val genres = listOf(Genres.RPG, Genres.SHOOTER, Genres.ADVENTURE, Genres.ACTION)

        val name2 = "Horizon Forbidden West"

        val req = Request(Method.GET, "/api/games?developer=Guerrilla-Games")

        // Act
        val games = api.routes(req)
        val rsp = getBody<GameListOutputModel>(games.bodyString())

        // Assert
        assertEquals(Status.OK, games.status)
        assertNotNull(rsp.games)
        assertEquals(2, rsp.games.size)
        assertEquals(
            listOf(
                GameOutputModel(idArgs.first(), name, developer, genres),
                GameOutputModel(idArgs.last(), name2, developer, genres)
            ),
            rsp.games
        )
    }

    @Test
    fun `gets a list of games according to its genre and developer and returns a response with 200`() {
        // Arrange
        val name = "Horizon Zero Dawn"
        val developer = "Guerrilla Games"
        val genres = listOf(Genres.RPG, Genres.SHOOTER, Genres.ADVENTURE, Genres.ACTION)

        val name2 = "Horizon Forbidden West"

        val req = Request(Method.GET, "/api/games?genres=rpg,shooter,adventure,action&developer=Guerrilla-Games")

        // Act
        val games = api.routes(req)
        val rsp = getBody<GameListOutputModel>(games.bodyString())

        // Assert
        assertEquals(Status.OK, games.status)
        assertNotNull(rsp.games)
        assertEquals(2, rsp.games.size)
        assertEquals(
            listOf(
                GameOutputModel(idArgs.first(), name, developer, genres),
                GameOutputModel(idArgs.last(), name2, developer, genres)
            ),
            rsp.games
        )
    }

    @Test
    fun `gets an empty list of games according to its genre and developer and returns a response with 200`() {
        // Arrange
        val req = Request(Method.GET, "/api/games?genres=rpg,shooter&developer=I-dont-make-games")

        // Act
        val games = api.routes(req)
        val rsp = getBody<GameListOutputModel>(games.bodyString())

        // Assert
        assertEquals(Status.OK, games.status)
        assertTrue(rsp.games.isEmpty())
    }

    @Test
    fun `tries to get a of games according to its genre and invalid developer and returns a response with 400`() {
        // Arrange
        val req = Request(Method.GET, "/api/games?genres=rpg,shooter&developer=Guerrilla-Games-Productions-Entertainment-Enterprises")

        // Act
        val rsp = api.routes(req)
        val body = getBody<ApiException>(rsp.bodyString())

        // Assert
        assertEquals(Status.BAD_REQUEST, rsp.status)
        assertFailsWith<Exception> {
            getBody<GameListOutputModel>(rsp.bodyString())
        }
        assertEquals(InvalidDeveloperNameLengthException.message, body.message)
    }

    @Test
    fun `gets a game by its name and returns a response with 200`() {
        // Arrange
        val name = "Horizon-Zero-Dawn"
        
        val reqGame = Request(Method.GET, "/api/game/${idArgs.first()}")
        val rspGame = api.routes(reqGame)
        val game = getBody<GameOutputModel>(rspGame.bodyString())

        val req = Request(Method.GET, "/api/games/?name=$name")

        // Act
        val gm = api.routes(req)
        val rsp = getBody<GameListOutputModel>(gm.bodyString())

        // Assert
        assertEquals(Status.OK, gm.status)
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
        val req = Request(Method.GET, "/api/games?name=Game-With-Big-Big-Big-Big-Big-Big-Big-Big-Big-Big-Big-Big-Big-Name")

        // Act
        val rsp = api.routes(req)
        val body = getBody<ApiException>(rsp.bodyString())

        // Assert
        assertEquals(Status.BAD_REQUEST, rsp.status)
        assertFailsWith<Exception> {
            getBody<GameListOutputModel>(rsp.bodyString())
        }
        assertEquals(InvalidGameNameLengthException.message, body.message)
    }

    @Test
    fun `gets game by its name with no capital letters and returns a response with 200`() {
        // Arrange
        val name = "Horizon-Zero-Dawn"

        val reqGame = Request(Method.GET, "/api/game/${idArgs.first()}")
        val rspGame = api.routes(reqGame)
        val game = getBody<GameOutputModel>(rspGame.bodyString())

        val nameSearched = name.lowercase()
        val req = Request(Method.GET, "/api/games/?name=$nameSearched")

        // Act
        val gm = api.routes(req)
        val rsp = getBody<GameListOutputModel>(gm.bodyString())

        // Assert
        assertEquals(Status.OK, gm.status)
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
        val reqGame1 = Request(Method.GET, "/api/game/${idArgs.first()}")
        val rspGame1 = api.routes(reqGame1)
        val game1 = getBody<GameOutputModel>(rspGame1.bodyString())

        val reqGame2 = Request(Method.GET, "/api/game/${idArgs.last()}")
        val rspGame2 = api.routes(reqGame2)
        val game2 = getBody<GameOutputModel>(rspGame2.bodyString())

        val searchedName = "horizon"
        val req = Request(Method.GET, "/api/games/?name=$searchedName")

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

        val req = Request(Method.GET, "/api/games/?name=$fakeGame")

        // Act
        val gm = api.routes(req)
        val rsp = getBody<GameListOutputModel>(gm.bodyString())

        // Assert
        assertEquals(Status.OK, gm.status)
        assertEquals(emptyList(), rsp.games)
    }

    @Test
    fun `gets a list of games according to its name and genre and returns a response with 200`() {
        // Arrange
        val name = "Horizon Zero Dawn"
        val developer = "Guerrilla Games"
        val genres = listOf(Genres.RPG, Genres.SHOOTER, Genres.ADVENTURE, Genres.ACTION)

        val req = Request(Method.GET, "/api/games?name=Horizon-Zero-Dawn&genres=action")

        // Act
        val games = api.routes(req)
        val rsp = getBody<GameListOutputModel>(games.bodyString())

        // Assert
        assertEquals(Status.OK, games.status)
        assertNotNull(rsp.games)
        assertEquals(1, rsp.games.size)
        assertEquals(
            listOf(
                GameOutputModel(idArgs.first(), name, developer, genres),
            ),
            rsp.games
        )
    }

    @Test
    fun `gets a list of games according to its name and developer and returns a response with 200`() {
        // Arrange
        val developer = "Guerrilla Games"
        val genres = listOf(Genres.RPG, Genres.SHOOTER, Genres.ADVENTURE, Genres.ACTION)

        val name2 = "Horizon Forbidden West"

        val req = Request(Method.GET, "/api/games?name=Horizon-Forbidden-West&developer=Guerrilla-Games")

        // Act
        val games = api.routes(req)
        val rsp = getBody<GameListOutputModel>(games.bodyString())

        // Assert
        assertEquals(Status.OK, games.status)
        assertNotNull(rsp.games)
        assertEquals(1, rsp.games.size)
        assertEquals(
            listOf(
                GameOutputModel(idArgs.last(), name2, developer, genres),
            ),
            rsp.games
        )
    }

    @Test
    fun `gets a list of games according to its name, with invalid format and returns a response with 400`() {
        // Arrange
        val req = Request(Method.GET, "/api/games?name=H F W")

        // Act
        val rsp = api.routes(req)
        val body = getBody<ApiException>(rsp.bodyString())

        // Assert
        assertEquals(Status.BAD_REQUEST, rsp.status)
        assertFailsWith<Exception> {
            getBody<GameListOutputModel>(rsp.bodyString())
        }
        assertEquals(InvalidGameNameException.message, body.message)
    }

    @Test
    fun `gets a list of games according to its developer, with invalid format and returns a response with 400`() {
        // Arrange
        val req = Request(Method.GET, "/api/games?developer=Guerrilla Games")

        // Act
        val rsp = api.routes(req)
        val body = getBody<ApiException>(rsp.bodyString())

        // Assert
        assertEquals(Status.BAD_REQUEST, rsp.status)
        assertFailsWith<Exception> {
            getBody<GameListOutputModel>(rsp.bodyString())
        }
        assertEquals(InvalidDeveloperNameException.message, body.message)
    }

    companion object {
        var playerToken: String
        val idArgs = arrayListOf<Int>()

        init {
            clearDataBase()
            val playerData = createAndGetPlayerToken(api.playerApi)
            playerToken = playerData.token
            createBaseGames()
        }

        private fun createBaseGames() {
            val reqCreate = Request(Method.POST, "/api/game")
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
            idArgs.add(id1.id)

            val reqCreate2 = Request(Method.POST, "/api/game")
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
            idArgs.add(id2.id)
        }
    }
}