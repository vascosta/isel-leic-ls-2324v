package pt.isel.ls.unit.game

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test
import pt.isel.ls.game.dataMem.GameDataMem
import pt.isel.ls.game.domain.GameOutputModel
import pt.isel.ls.game.domain.GameSearchQuery
import pt.isel.ls.game.domain.Genres
import pt.isel.ls.game.domain.Genres.Companion.toGenre
import pt.isel.ls.game.services.GameServices
import pt.isel.ls.utils.GameAlreadyExistsException
import pt.isel.ls.utils.GameNotFoundException
import pt.isel.ls.utils.InvalidDeveloperNameException
import pt.isel.ls.utils.InvalidDeveloperNameLengthException
import pt.isel.ls.utils.InvalidGameIdException
import pt.isel.ls.utils.InvalidGameNameException
import pt.isel.ls.utils.InvalidGameNameLengthException
import pt.isel.ls.utils.InvalidGenreException
import pt.isel.ls.utils.PAGING_BUILT_IN_LIMIT
import pt.isel.ls.utils.Paging.Companion.DEFAULT_SKIP
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class GameServicesTests {

    private val dataMem = GameDataMem()
    private val services = GameServices(dataMem)

    @Test
    fun `creates a new game and returns its id`() {
        // Arrange
        val name = "New Super Mario Bros"
        val developer = "Nintendo"
        val genres = listOf(Genres.ACTION)

        // Act
        val game = services.createGame(name, developer, genres)

        // Assert
        assertNotNull(game)
        assertEquals(1, game.id)
    }

    @Test
    fun `tries to create a new game with invalid name and throws an exception`() {
        // Arrange
        val name = "New Super *Mario Bros"
        val developer = "Nintendo"
        val genres = listOf(Genres.ACTION)

        // Assert
        assertFailsWith<InvalidGameNameException> { services.createGame(name, developer, genres) }
    }

    @Test
    fun `tries to create a new game with invalid developer and throws an exception`() {
        // Arrange
        val name = "New Super Mario Bros"
        val developer = "Fake Nintendo Company With a Huge Name"
        val genres = listOf(Genres.ACTION)

        // Assert
        assertFailsWith<InvalidDeveloperNameLengthException> { services.createGame(name, developer, genres) }
    }

    @Test
    fun `tries to create a new game with invalid genres and throws an exception`() {
        // Arrange
        val name = "New Super Mario Bros"
        val developer = "Nintendo"

        // Assert
        assertFailsWith<InvalidGenreException> {
            services.createGame(name, developer, listOf(toGenre("Mystery")))
        }
    }

    @Test
    fun `tries to create a game that already exists and throws an exception`() {
        // Arrange
        val name = "New Super Mario Bros"
        val developer = "Nintendo"
        val genres = listOf(Genres.ACTION)

        // Act
        val game = services.createGame(name, developer, genres)

        // Assert
        assertNotNull(game)
        assertEquals(1, game.id)

        assertFailsWith<GameAlreadyExistsException> {
            services.createGame(name, developer, genres)
        }
    }

    @Test
    fun `gets the details of game according to its id and returns its details`() {
        // Arrange
        val name = "Call Of Duty Modern Warfare"
        val developer = "Infinity Ward"
        val genres = listOf(Genres.SHOOTER, Genres.ACTION)
        val id = services.createGame(name, developer, genres)

        // Act
        assertNotNull(id)
        val game = services.getGameDetails(id.id)

        // Assert
        assertNotNull(game)
        assertEquals(id.id, game.id)
        assertEquals(name, game.name)
        assertEquals(developer, game.developer)
        assertEquals(genres, game.genres)
    }

    @Test
    fun `tries to get details of a game that doesn't exist and throws an exception`() {
        // Arrange
        val id = 1

        // Assert
        assertFailsWith<GameNotFoundException> { services.getGameDetails(id) }
    }

    @Test
    fun `tries to get details of a game with invalid id and throws an exception`() {
        // Arrange
        val id = - 1

        // Assert
        assertFailsWith<InvalidGameIdException> { services.getGameDetails(id) }
    }

    @Test
    fun `gets a list of games according to its genre and returns its details`() {
        // Arrange
        val name = "Horizon Zero Dawn"
        val developer = "Guerrilla Games"
        val genres = listOf(Genres.RPG, Genres.SHOOTER, Genres.ADVENTURE, Genres.ACTION)
        val id = services.createGame(name, developer, genres)

        val name2 = "Killzone Shadow Fall"
        val genres2 = listOf(Genres.SHOOTER)
        val id2 = services.createGame(name2, developer, genres2)

        // Act
        val games = services.getGames(
            GameSearchQuery(DEFAULT_SKIP, PAGING_BUILT_IN_LIMIT, listOf(Genres.ADVENTURE), null, null)
        )

        // Assert
        assertNotNull(games)
        assertTrue(games.games.isNotEmpty())
        assertEquals(
            listOf(GameOutputModel(id.id, name, developer, genres)),
            games.games
        )
    }

    @Test
    fun `gets a list of games according to its developer and returns its details`() {
        // Arrange
        val name = "Horizon Zero Dawn"
        val developer = "Guerrilla Games"
        val genres = listOf(Genres.RPG, Genres.SHOOTER, Genres.ADVENTURE, Genres.ACTION)
        val id = services.createGame(name, developer, genres)

        val name2 = "Killzone Shadow Fall"
        val genres2 = listOf(Genres.SHOOTER)
        val id2 = services.createGame(name2, developer, genres2)

        // Act
        val games = services.getGames(
            GameSearchQuery(DEFAULT_SKIP, PAGING_BUILT_IN_LIMIT, null, developer.toStoredName(), null)
        )

        // Assert
        assertNotNull(games)
        assertTrue(games.games.isNotEmpty())
        assertEquals(
            listOf(GameOutputModel(id.id, name, developer, genres), GameOutputModel(id2.id, name2, developer, genres2)),
            games.games
        )
    }

    @Test
    fun `gets a list of games according to its genre and developer and returns its details`() {
        // Arrange
        val name = "Horizon Zero Dawn"
        val developer = "Guerrilla Games"
        val genres = listOf(Genres.RPG, Genres.SHOOTER, Genres.ADVENTURE, Genres.ACTION)
        val id = services.createGame(name, developer, genres)

        val name3 = "Killzone Shadow Fall"
        val genres2 = listOf(Genres.SHOOTER)
        val id3 = services.createGame(name3, developer, genres2)

        // Act
        val games = services.getGames(GameSearchQuery(DEFAULT_SKIP, PAGING_BUILT_IN_LIMIT, genres, developer.toStoredName(), null))
        val games2 = services.getGames(GameSearchQuery(DEFAULT_SKIP, PAGING_BUILT_IN_LIMIT, genres2, developer.toStoredName(), null))

        // Assert
        assertNotNull(games)
        assertTrue(games.games.isNotEmpty())
        assertEquals(
            listOf(GameOutputModel(id.id, name, developer, genres)),
            games.games
        )

        assertNotNull(games2)
        assertTrue(games2.games.isNotEmpty())
        assertEquals(
            listOf(
                GameOutputModel(id.id, name, developer, genres),
                GameOutputModel(id3.id, name3, developer, genres2)
            ),
            games2.games
        )
    }

    @Test
    fun `tries to get a list of games according to its genre and developer and returns an empty list`() {
        // Arrange
        val developer = "Sucker Punch"
        val genres = listOf(Genres.RPG, Genres.ADVENTURE, Genres.ACTION)

        // Act
        val games = services.getGames(GameSearchQuery(DEFAULT_SKIP, PAGING_BUILT_IN_LIMIT, genres, developer.toStoredName(), null))

        // Assert
        assertNotNull(games)
        assertTrue(games.games.isEmpty())
    }

    @Test
    fun `tries to get a list of games according to its genre and invalid developer and throws an exception`() {
        // Arrange
        val developer = "Sucker Punch Productions is a video game developer"
        val genres = listOf(Genres.RPG, Genres.ADVENTURE, Genres.ACTION)

        // Assert
        assertFailsWith<InvalidDeveloperNameLengthException> {
            services.getGames(GameSearchQuery(DEFAULT_SKIP, PAGING_BUILT_IN_LIMIT, genres, developer.toStoredName(), null))
        }
    }

    @Test
    fun `gets a game by name and returns its details`() {
        // Arrange
        val name = "The Witcher 3"
        val developer = "CD Projekt"
        val genres = listOf(Genres.RPG, Genres.ADVENTURE, Genres.ACTION)
        services.createGame(name, developer, genres)

        // Act
        val games = services.getGames(
            GameSearchQuery(DEFAULT_SKIP, PAGING_BUILT_IN_LIMIT, null, null, name.toStoredName())
        )
        val game = games.games[0]

        // Assert
        assertNotNull(games)
        assertEquals(listOf(GameOutputModel(game.id, game.name, game.developer, game.genres)), games.games)
        assertEquals(GameOutputModel(game.id, game.name, game.developer, game.genres), game)
    }

    @Test
    fun `tries to get a list of games by its invalid name and returns a response with 400`() {
        // Arrange
        val name = "Game-With-Big-Big-Big-Big-Big-Big-Big-Big-Big-Big-Big-Big-Big-Name"

        // Assert
        assertFailsWith<InvalidGameNameLengthException> {
            services.getGames(GameSearchQuery(DEFAULT_SKIP, PAGING_BUILT_IN_LIMIT, null, null, name))
        }
    }

    @Test
    fun `gets two games by its partial name and returns its details`() {
        // Arrange
        val gameName1 = "Resident Evil 4"
        val gameName2 = "Resident Evil 2"
        val developer = "Capcom"
        val genres = listOf(Genres.HORROR)
        services.createGame(gameName1, developer, genres)
        services.createGame(gameName2, developer, genres)

        val searchedName = "resident"

        // Act
        val games = services.getGames(
            GameSearchQuery(DEFAULT_SKIP, PAGING_BUILT_IN_LIMIT, null, null, searchedName)
        )

        // Assert
        assertEquals(2, games.games.size)
        val game1 = games.games[0]
        val game2 = games.games[1]
        assertEquals(
            listOf(
                GameOutputModel(game1.id, game1.name, game1.developer, game1.genres),
                GameOutputModel(game2.id, game2.name, game2.developer, game2.genres)
            ),
            games.games
        )
    }

    @Test
    fun `gets game by its name with no capital letters and returns its details`() {
        // Arrange
        val name = "The Witcher 3"
        val developer = "CD Projekt"
        val genres = listOf(Genres.RPG, Genres.ADVENTURE, Genres.ACTION)
        services.createGame(name, developer, genres)
        val nameSearched = name.lowercase()

        // Act
        val games = services.getGames(
            GameSearchQuery(DEFAULT_SKIP, PAGING_BUILT_IN_LIMIT, null, null, nameSearched.toStoredName())
        )

        // Assert
        assertEquals(1, games.games.size)
        val game = games.games[0]
        assertEquals(listOf(GameOutputModel(game.id, game.name, game.developer, game.genres)), games.games)
        assertEquals(GameOutputModel(game.id, game.name, game.developer, game.genres), game)
    }

    @Test
    fun `tries to get non existing game by name and returns null`() {
        // Arrange
        val fakeGame = "Uno"

        // Act
        val games = services.getGames(
            GameSearchQuery(DEFAULT_SKIP, PAGING_BUILT_IN_LIMIT, null, null, fakeGame)
        )

        // Assert
        assertEquals(emptyList<GameOutputModel>(), games.games)
    }

    @Test
    fun `gets a list of games according to its name and genre and returns its details`() {
        // Arrange
        val name = "Horizon Zero Dawn"
        val developer = "Guerrilla Games"
        val genres = listOf(Genres.RPG, Genres.SHOOTER, Genres.ADVENTURE, Genres.ACTION)
        val id = services.createGame(name, developer, genres)

        val name2 = "Killzone Shadow Fall"
        val genres2 = listOf(Genres.SHOOTER)
        val id2 = services.createGame(name2, developer, genres2)

        // Act
        val games = services.getGames(
            GameSearchQuery(DEFAULT_SKIP, PAGING_BUILT_IN_LIMIT, listOf(Genres.ACTION), null, name.toStoredName())
        )
        val games2 = services.getGames(
            GameSearchQuery(DEFAULT_SKIP, PAGING_BUILT_IN_LIMIT, genres2, null, name2.toStoredName())
        )

        // Assert
        assertNotNull(games)
        assertTrue(games.games.isNotEmpty())
        assertEquals(
            listOf(GameOutputModel(id.id, name, developer, genres)),
            games.games
        )

        assertNotNull(games2)
        assertTrue(games2.games.isNotEmpty())
        assertEquals(
            listOf(
                GameOutputModel(id2.id, name2, developer, genres2)
            ),
            games2.games
        )
    }

    @Test
    fun `gets a list of games according to its name and developer and returns its details`() {
        // Arrange
        val name = "Horizon Zero Dawn"
        val developer = "Guerrilla Games"
        val genres = listOf(Genres.RPG, Genres.SHOOTER, Genres.ADVENTURE, Genres.ACTION)
        val id = services.createGame(name, developer, genres)

        val name2 = "Killzone Shadow Fall"
        val genres2 = listOf(Genres.SHOOTER)
        val id2 = services.createGame(name2, developer, genres2)

        // Act
        val games = services.getGames(
            GameSearchQuery(DEFAULT_SKIP, PAGING_BUILT_IN_LIMIT, null, developer.toStoredName(), name.toStoredName())
        )
        val games2 = services.getGames(
            GameSearchQuery(DEFAULT_SKIP, PAGING_BUILT_IN_LIMIT, null, developer.toStoredName(), name2.toStoredName())
        )

        // Assert
        assertNotNull(games)
        assertTrue(games.games.isNotEmpty())
        assertEquals(
            listOf(GameOutputModel(id.id, name, developer, genres)),
            games.games
        )

        assertNotNull(games2)
        assertTrue(games2.games.isNotEmpty())
        assertEquals(
            listOf(
                GameOutputModel(id2.id, name2, developer, genres2)
            ),
            games2.games
        )
    }

    @Test
    fun `gets a list of games according to its name, with invalid format and returns a response with 400`() {
        // Arrange
        val name = "Horizon Forbidden West"

        // Assert
        assertFailsWith<InvalidGameNameException> {
            services.getGames(GameSearchQuery(DEFAULT_SKIP, PAGING_BUILT_IN_LIMIT, null, null, name))
        }
    }

    @Test
    fun `gets a list of games according to its developer with invalid format and returns a response with 400`() {
        // Arrange
        val developer = "Guerilla Games"

        // Assert
        assertFailsWith<InvalidDeveloperNameException> {
            services.getGames(GameSearchQuery(DEFAULT_SKIP, PAGING_BUILT_IN_LIMIT, null, developer, null))
        }
    }

    private fun String.toStoredName() = this.replace(" ", "-")
}
