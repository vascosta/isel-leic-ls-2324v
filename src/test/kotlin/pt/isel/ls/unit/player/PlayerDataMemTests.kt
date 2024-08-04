package pt.isel.ls.unit.player

import org.junit.Assert.assertNotEquals
import org.junit.Test
import pt.isel.ls.player.dataMem.PlayerDataMem
import pt.isel.ls.utils.Paging
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PlayerDataMemTests {

    private val dataMem = PlayerDataMem()

    @Test
    fun `creates a player and then returns his token and id`() {
        // Arrange
        val name = "Maria"
        val email = "maria@email.com"
        val password = "password"

        // Act
        val playerAuth = dataMem.createPlayer(name, email, password)

        // Assert
        assertNotNull(playerAuth.token)
        assertNotNull(playerAuth.id)
    }

    @Test
    fun `login a player by name and password and then returns his token and id`() {
        // Arrange
        val name = "Maria"
        val email = "maria@email.com"
        val password = "Password1"
        val playerAuth = dataMem.createPlayer(name, email, password)

        // Act
        val login = dataMem.login(name, password)

        // Assert
        assertNotNull(login)
        assertNotEquals(playerAuth.token, login.token)
        assertEquals(playerAuth.id, login.id)
    }

    @Test
    fun `try login a player that does not exist by name and password and then returns null`() {
        // Arrange
        val name = "Maria"
        val password = "Password1"

        // Act
        val login = dataMem.login(name, password)

        // Assert
        assertNull(login)
    }

    @Test
    fun `logout a player by id`() {
        // Arrange
        val name = "Maria"
        val email = "maria@email.com"
        val password = "Password1"
        dataMem.createPlayer(name, email, password)
        val login = dataMem.login(name, password)

        // Act
        dataMem.logout(login!!.id)
        val playerHome = dataMem.getPlayerHome(login.token)

        // Assert
        assertNull(playerHome)
    }

    @Test
    fun `gets a player home by token and then returns his id and name`() {
        // Arrange
        val name = "Joaquim"
        val email = "joaquim@email.com"
        val password = "password"
        val playerAuth = dataMem.createPlayer(name, email, password)

        // Act
        val playerHome = dataMem.getPlayerHome(playerAuth.token)

        // Assert
        assertNotNull(playerHome)
        assertEquals(playerAuth.id, playerHome.id)
        assertEquals(name, playerHome.name)
    }

    @Test
    fun `try getting a player home that does not exist by token and then returns null`() {
        // Arrange
        val token = UUID.randomUUID().toString()

        // Act
        val playerInfo = dataMem.getPlayerHome(token)

        // Assert
        assertNull(playerInfo)
    }

    @Test
    fun `gets a player id by token and then returns his id`() {
        // Arrange
        val name = "Joaquim"
        val email = "joaquim@email.com"
        val password = "password"
        val playerAuth = dataMem.createPlayer(name, email, password)

        // Act
        val playerId = dataMem.getPlayerIdByToken(playerAuth.token)

        // Assert
        assertNotNull(playerId)
        assertEquals(playerAuth.id, playerId)
    }

    @Test
    fun `try getting a player id that does not exist by token and then returns null`() {
        // Arrange
        val token = UUID.randomUUID().toString()

        // Act
        val playerInfo = dataMem.getPlayerIdByToken(token)

        // Assert
        assertNull(playerInfo)
    }

    @Test
    fun `gets a player info by id and then returns his id, name and email`() {
        // Arrange
        val name = "Joaquim"
        val email = "joaquim@email.com"
        val password = "password"
        val id = dataMem.createPlayer(name, email, password).id

        // Act
        val playerInfo = dataMem.getPlayerInfo(id)

        // Assert
        assertNotNull(playerInfo)
        assertEquals(id, playerInfo.id)
        assertEquals("Joaquim", playerInfo.name)
        assertEquals("joaquim@email.com", playerInfo.email)
    }

    @Test
    fun `try getting a player info that does not exist by id and then returns null`() {
        // Arrange
        val id = 1

        // Act
        val playerInfo = dataMem.getPlayerInfo(id)

        // Assert
        assertNull(playerInfo)
    }

    @Test
    fun `searches for players info by name and then returns a list with players info`() {
        // Arrange
        val name = "Joaquim"
        val email = "joaquim@email.com"
        val password = "password"
        dataMem.createPlayer(name, email, password)

        // Act
        val players = dataMem.searchPlayersByName(name, Paging.DEFAULT_LIMIT, Paging.DEFAULT_SKIP)

        // Assert
        assertEquals(1, players.players.size)
        assertEquals("Joaquim", players.players[0].name)
        assertEquals("joaquim@email.com", players.players[0].email)
    }

    @Test
    fun `try searching for players info that does not exist by name and then returns an empty list`() {
        // Arrange
        val name = "Paulo"

        // Act
        val players = dataMem.searchPlayersByName(name, Paging.DEFAULT_LIMIT, Paging.DEFAULT_SKIP)

        // Assert
        assertTrue(players.players.isEmpty())
    }

    @Test
    fun `checks if a player already exists and then returns true`() {
        // Arrange
        val name = "Manuel"
        val email = "manuel@email.com"
        val password = "password"
        dataMem.createPlayer(name, email, password)

        // Act
        val playerAlreadyExists = dataMem.checkIfPlayerExists(name)

        // Assert
        assertTrue(playerAlreadyExists)
    }

    @Test
    fun `checks if a player already exists and then returns false`() {
        // Arrange
        val name = "João"

        // Act
        val playerAlreadyExists = dataMem.checkIfPlayerExists(name)

        // Assert
        assertFalse(playerAlreadyExists)
    }

    @Test
    fun `checks if a player token exists and then returns true`() {
        // Arrange
        val name = "Manuel"
        val email = "manuel@email.com"
        val password = "password"
        val token = dataMem.createPlayer(name, email, password).token

        // Act
        val playerTokenExists = dataMem.checkIfPlayerTokenExists(token)

        // Assert
        assertTrue(playerTokenExists)
    }

    @Test
    fun `checks if a player token exists and then returns false`() {
        // Arrange
        val token = UUID.randomUUID().toString()

        // Act
        val playerTokenExists = dataMem.checkIfPlayerTokenExists(token)

        // Assert
        assertFalse(playerTokenExists)
    }
}
