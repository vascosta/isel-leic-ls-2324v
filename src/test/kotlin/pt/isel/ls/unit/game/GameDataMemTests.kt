package pt.isel.ls.unit.game

import org.junit.Test
import pt.isel.ls.game.dataMem.GameDataMem
import pt.isel.ls.game.domain.GameOutputModel
import pt.isel.ls.game.domain.GameSearchQuery
import pt.isel.ls.game.domain.Genres
import pt.isel.ls.utils.InvalidGenreException
import pt.isel.ls.utils.PAGING_BUILT_IN_LIMIT
import pt.isel.ls.utils.Paging.Companion.DEFAULT_SKIP
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GameDataMemTests {

    private val dataMem = GameDataMem()

    @Test
    fun `creates a new game and returns its id`() {
        // Arrange
        val name = "New Super Mario Bros"
        val developer = "Nintendo"
        val genres = listOf(Genres.ACTION)

        // Act
        val game = dataMem.createGame(name, developer, genres)

        // Assert
        assertNotNull(game)
        assertEquals(1, game.id)
    }

    @Test
    fun `tries to create a new game with invalid genres and throws an exception`() {
        // Arrange
        val name = "New Super Mario Bros"
        val developer = "Nintendo"

        // Assert
        assertFailsWith<InvalidGenreException> {
            dataMem.createGame(name, developer, listOf(Genres.toGenre("Mystery")))
        }
    }

    @Test
    fun `gets the details of game according to its id and returns its details`() {
        // Arrange
        val name = "Call Of Duty Modern Warfare"
        val developer = "Infinity Ward"
        val genres = listOf(Genres.SHOOTER, Genres.ACTION)
        val id = dataMem.createGame(name, developer, genres)

        // Act
        val game = dataMem.getGameDetails(id.id)

        // Assert
        assertNotNull(game)
        assertEquals(id.id, game.id)
        assertEquals(name, game.name)
        assertEquals(developer, game.developer)
        assertEquals(genres, game.genres)
    }

    @Test
    fun `tries to get details of a game that doesn't exist and returns null`() {
        // Arrange
        val id = 1

        // Assert
        assertNull(dataMem.getGameDetails(id))
    }

    @Test
    fun `gets a list of games according to its genre and returns its details`() {
        // Arrange
        val name = "Horizon Zero Dawn"
        val developer = "Guerrilla Games"
        val genres = listOf(Genres.RPG, Genres.SHOOTER, Genres.ADVENTURE, Genres.ACTION)
        val id = dataMem.createGame(name, developer, genres)

        val name2 = "Killzone Shadow Fall"
        val genres2 = listOf(Genres.SHOOTER)
        val id2 = dataMem.createGame(name2, developer, genres2)

        // Act
        val games = dataMem.getGames(
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
        val id = dataMem.createGame(name, developer, genres)

        val name2 = "Killzone Shadow Fall"
        val genres2 = listOf(Genres.SHOOTER)
        val id2 = dataMem.createGame(name2, developer, genres2)

        // Act
        val games = dataMem.getGames(
            GameSearchQuery(DEFAULT_SKIP, PAGING_BUILT_IN_LIMIT, null, developer, null)
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
        val id = dataMem.createGame(name, developer, genres)

        val name2 = "Killzone Shadow Fall"
        val genres2 = listOf(Genres.SHOOTER)
        val id2 = dataMem.createGame(name2, developer, genres2)

        // Act
        val games = dataMem.getGames(
            GameSearchQuery(DEFAULT_SKIP, PAGING_BUILT_IN_LIMIT, genres, developer, null)
        )
        val games2 = dataMem.getGames(
            GameSearchQuery(DEFAULT_SKIP, PAGING_BUILT_IN_LIMIT, genres2, developer, null)
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
                GameOutputModel(id.id, name, developer, genres),
                GameOutputModel(id2.id, name2, developer, genres2)
            ),
            games2.games
        )
    }

    @Test
    fun `tries to get a list of games according to its genre and developer and returns an empty list`() {
        // Arrange
        val developer = "Sucker Punch Productions"
        val genres = listOf(Genres.RPG, Genres.ADVENTURE, Genres.ACTION)

        // Act
        val games = dataMem.getGames(
            GameSearchQuery(DEFAULT_SKIP, PAGING_BUILT_IN_LIMIT, genres, developer, null)
        )

        // Assert
        assertNotNull(games)
        assertTrue(games.games.isEmpty())
    }

    @Test
    fun `gets a game by its name and returns its details`() {
        // Arrange
        val name = "The Witcher 3"
        val developer = "CD Projekt"
        val genres = listOf(Genres.RPG, Genres.ADVENTURE, Genres.ACTION)
        dataMem.createGame(name, developer, genres)

        // Act
        val games = dataMem.getGames(
            GameSearchQuery(
                DEFAULT_SKIP, PAGING_BUILT_IN_LIMIT, null, null, name
            )
        )

        // Assert
        assertEquals(1, games.games.size)
        val game = games.games[0]
        assertEquals(listOf(GameOutputModel(game.id, game.name, game.developer, game.genres)), games.games)
        assertEquals(GameOutputModel(game.id, game.name, game.developer, game.genres), game)
    }

    @Test
    fun `gets two games by its partial name and returns its details`() {
        // Arrange
        val gameName1 = "Resident Evil 4"
        val gameName2 = "Resident Evil 2"
        val developer = "Capcom"
        val genres = listOf(Genres.HORROR)
        dataMem.createGame(gameName1, developer, genres)
        dataMem.createGame(gameName2, developer, genres)

        val searchedName = "resident"

        // Act
        val games = dataMem.getGames(
            GameSearchQuery(
                DEFAULT_SKIP, PAGING_BUILT_IN_LIMIT, null, null, searchedName
            )
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
        dataMem.createGame(name, developer, genres)
        val searchedName = name.lowercase()

        // Act
        val games = dataMem.getGames(
            GameSearchQuery(
                DEFAULT_SKIP, PAGING_BUILT_IN_LIMIT, null, null, searchedName
            )
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
        val invalidName = dataMem.getGames(
            GameSearchQuery(
                DEFAULT_SKIP, PAGING_BUILT_IN_LIMIT, null, null, fakeGame
            )
        )

        // Assert
        assertEquals(emptyList(), invalidName.games)
    }

    @Test
    fun `gets a list of games according to its name and genre and returns its details`() {
        // Arrange
        val name = "Horizon Zero Dawn"
        val developer = "Guerrilla Games"
        val genres = listOf(Genres.RPG, Genres.SHOOTER, Genres.ADVENTURE, Genres.ACTION)
        val id = dataMem.createGame(name, developer, genres)

        val name2 = "Killzone Shadow Fall"
        val genres2 = listOf(Genres.SHOOTER)
        val id2 = dataMem.createGame(name2, developer, genres2)

        // Act
        val games = dataMem.getGames(
            GameSearchQuery(DEFAULT_SKIP, PAGING_BUILT_IN_LIMIT, listOf(Genres.ACTION), null, name)
        )
        val games2 = dataMem.getGames(
            GameSearchQuery(DEFAULT_SKIP, PAGING_BUILT_IN_LIMIT, genres2, null, name2)
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
        val id = dataMem.createGame(name, developer, genres)

        val name2 = "Killzone Shadow Fall"
        val genres2 = listOf(Genres.SHOOTER)
        val id2 = dataMem.createGame(name2, developer, genres2)

        // Act
        val games = dataMem.getGames(
            GameSearchQuery(DEFAULT_SKIP, PAGING_BUILT_IN_LIMIT, null, developer, name)
        )
        val games2 = dataMem.getGames(
            GameSearchQuery(DEFAULT_SKIP, PAGING_BUILT_IN_LIMIT, null, developer, name2)
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
    fun `checks if a game exists and returns true`() {
        // Arrange
        val name = "Ghost Of Tsushima"
        val developer = "Sucker Punch"
        val genres = listOf(Genres.ACTION, Genres.ADVENTURE, Genres.RPG)
        dataMem.createGame(name, developer, genres)

        // Assert
        assertTrue(dataMem.checkIfGameExists(name))
    }

    @Test
    fun `checks if a game exists and returns false`() {
        // Arrange
        val name = "Ghost Of Tsushima"

        // Assert
        assertFalse(dataMem.checkIfGameExists(name))
    }
}
