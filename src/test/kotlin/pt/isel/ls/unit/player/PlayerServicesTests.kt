package pt.isel.ls.unit.player

import org.junit.Assert.assertNotEquals
import org.junit.Test
import pt.isel.ls.player.dataMem.PlayerDataMem
import pt.isel.ls.player.services.PlayerServices
import pt.isel.ls.utils.InvalidEmailException
import pt.isel.ls.utils.InvalidPasswordException
import pt.isel.ls.utils.InvalidPlayerIdException
import pt.isel.ls.utils.InvalidPlayerNameException
import pt.isel.ls.utils.Paging
import pt.isel.ls.utils.PlayerAlreadyExistsException
import pt.isel.ls.utils.PlayerNotFoundException
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PlayerServicesTests {

    private val services = PlayerServices(PlayerDataMem())

    @Test
    fun `creates a player and then returns his token and id`() {
        // Arrange
        val name = "Maria"
        val email = "maria@email.com"
        val password = "Pasword1"

        // Act
        val playerAuth = services.createPlayer(name, email, password)

        // Assert
        assertNotNull(playerAuth.token)
        assertNotNull(playerAuth.id)
    }

    @Test
    fun `try creating a player with invalid parameters and then throws an exception`() {
        // Arrange
        val validName = "Manuel"
        val invalidName = "Manuel Silva"
        val validEmail = "manuel@email.com"
        val invalidEmail = "manuel@"
        val validPassword = "Pasword1"
        val invalidPassword = "pass"

        // Act & Assert
        assertFailsWith<InvalidPlayerNameException> { services.createPlayer(invalidName, validEmail, validPassword) }
        assertFailsWith<InvalidEmailException> { services.createPlayer(validName, invalidEmail, validPassword) }
        assertFailsWith<InvalidPasswordException> { services.createPlayer(validName, validEmail, invalidPassword) }
    }

    @Test
    fun `try creating a player that already exists and then throws an exception`() {
        // Arrange
        val name = "Maria"
        val email = "maria@email.com"
        val password = "Pasword1"
        services.createPlayer(name, email, password)

        // Act & Assert
        assertFailsWith<PlayerAlreadyExistsException> { services.createPlayer(name, email, password) }
    }

    @Test
    fun `login a player by name and password and then returns his token and id`() {
        // Arrange
        val name = "Maria"
        val email = "maria@email.com"
        val password = "Password1"
        val playerAuth = services.createPlayer(name, email, password)

        // Act
        val login = services.login(name, password)

        // Assert
        assertNotNull(login)
        assertNotEquals(playerAuth.token, login.token)
        assertEquals(playerAuth.id, login.id)
    }

    @Test
    fun `try login a player that does not exist by name and password and then throws an exception`() {
        // Arrange
        val name = "Maria"
        val password = "Password1"

        // Act & Assert
        assertFailsWith<PlayerNotFoundException> { services.login(name, password) }
    }

    @Test
    fun `logout a player by id`() {
        // Arrange
        val name = "Maria"
        val email = "maria@email.com"
        val password = "Password1"
        services.createPlayer(name, email, password)
        val login = services.login(name, password)

        // Act
        services.logout(login.id)

        // Assert
        assertFailsWith<PlayerNotFoundException> { services.getPlayerHome(login.token) }
    }

    @Test
    fun `gets a player home by token and then returns his id and name`() {
        // Arrange
        val name = "Joaquim"
        val email = "joaquim@email.com"
        val password = "Pasword1"
        val playerAuth = services.createPlayer(name, email, password)

        // Act
        val playerHome = services.getPlayerHome(playerAuth.token)

        // Assert
        assertNotNull(playerHome)
        assertEquals(playerAuth.id, playerHome.id)
        assertEquals(name, playerHome.name)
    }

    @Test
    fun `try getting a player home that does not exist by token and then throws an exception`() {
        // Arrange
        val token = UUID.randomUUID().toString()

        // Act & Assert
        assertFailsWith<PlayerNotFoundException> { services.getPlayerHome(token) }
    }

    @Test
    fun `gets a player id by token and then returns his id`() {
        // Arrange
        val name = "Joaquim"
        val email = "joaquim@email.com"
        val password = "Pasword1"
        val playerAuth = services.createPlayer(name, email, password)

        // Act
        val playerId = services.getPlayerIdByToken(playerAuth.token)

        // Assert
        assertNotNull(playerId)
        assertEquals(playerAuth.id, playerId)
    }

    @Test
    fun `try getting a player id that does not exist by token and then returns null`() {
        // Arrange
        val token = UUID.randomUUID().toString()

        // Act
        val playerId = services.getPlayerIdByToken(token)

        // Assert
        assertNull(playerId)
    }

    @Test
    fun `gets a player info by id and then returns his id, name and email`() {
        // Arrange
        val name = "Joaquim"
        val email = "joaquim@email.com"
        val password = "Pasword1"
        val id = services.createPlayer(name, email, password).id

        // Act
        val playerInfo = services.getPlayerInfo(id)

        // Assert
        assertNotNull(playerInfo)
        assertEquals(id, playerInfo.id)
        assertEquals("Joaquim", playerInfo.name)
        assertEquals("joaquim@email.com", playerInfo.email)
    }

    @Test
    fun `try getting a player info with an invalid id and then throws an exception`() {
        // Arrange
        val id = -1

        // Act & Assert
        assertFailsWith<InvalidPlayerIdException> { services.getPlayerInfo(id) }
    }

    @Test
    fun `try getting a player info that does not exist by id and then throws an exception`() {
        // Arrange
        val id = 1

        // Act & Assert
        assertFailsWith<PlayerNotFoundException> { services.getPlayerInfo(id) }
    }

    @Test
    fun `searches for players info by name and then returns an list with players info`() {
        // Arrange
        val name = "Joaquim"
        val email = "joaquim@email.com"
        val password = "Pasword1"
        services.createPlayer(name, email, password)

        // Act
        val players = services.searchPlayersByName(name, Paging.DEFAULT_LIMIT, Paging.DEFAULT_SKIP)

        // Assert
        assertNotNull(players)
        assertEquals("Joaquim", players.players[0].name)
        assertEquals("joaquim@email.com", players.players[0].email)
    }

    @Test
    fun `try searching for players info with an invalid name and then throws an exception`() {
        // Arrange
        val name1 = "JoaquimSilvadasCouvesAlmeida"
        val name2 = ""

        // Act & Assert
        assertFailsWith<InvalidPlayerNameException> { services.searchPlayersByName(name1, Paging.DEFAULT_LIMIT, Paging.DEFAULT_SKIP) }
        assertFailsWith<InvalidPlayerNameException> { services.searchPlayersByName(name2, Paging.DEFAULT_LIMIT, Paging.DEFAULT_SKIP) }
    }

    @Test
    fun `try searching for players info that does not exist by name and then returns an empty list`() {
        // Arrange
        val name = "Paulo"

        // Act
        val players = services.searchPlayersByName(name, Paging.DEFAULT_LIMIT, Paging.DEFAULT_SKIP)

        // Assert
        assertEquals(0, players.players.size)
    }

    @Test
    fun `checks if a player token exists and then returns true`() {
        // Arrange
        val name = "Manuel"
        val email = "manuel@email.com"
        val password = "Pasword1"
        val token = services.createPlayer(name, email, password).token

        // Act
        val playerTokenExists = services.checkIfPlayerTokenExists(token)

        // Assert
        assertTrue(playerTokenExists)
    }

    @Test
    fun `checks if a player token exists and then returns false`() {
        // Arrange
        val token = UUID.randomUUID().toString()

        // Act
        val playerTokenExists = services.checkIfPlayerTokenExists(token)

        // Assert
        assertFalse(playerTokenExists)
    }
}
