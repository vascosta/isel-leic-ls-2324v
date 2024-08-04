package pt.isel.ls.player.dataMem

import pt.isel.ls.player.domain.CreatePlayerOutputModel
import pt.isel.ls.player.domain.GetPlayerHomeOutputModel
import pt.isel.ls.player.domain.GetPlayerInfoOutputModel
import pt.isel.ls.player.domain.LoginOutputModel
import pt.isel.ls.player.domain.Player
import pt.isel.ls.player.domain.SearchPlayersByNameOutputModel
import pt.isel.ls.utils.decode
import pt.isel.ls.utils.encode
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class PlayerDataMem : PlayerDataAccess {

    private val players = ConcurrentHashMap<Int, Player>()

    override fun createPlayer(name: String, email: String, password: String): CreatePlayerOutputModel {
        val id = players.size + 1
        val token = UUID.randomUUID().toString()
        val tokenHash = encode(token)
        val passwordHash = encode(password)
        players[id] = Player(id, name, email, passwordHash, tokenHash)
        return CreatePlayerOutputModel(token, id)
    }

    override fun login(name: String, password: String): LoginOutputModel? {
        val player = players.values.find { it.name == name } ?: return null
        if (decode(player.password) == password) {
            val newTokenHash = encode(UUID.randomUUID().toString())
            val updatedPlayer = player.copy(tokenHash = newTokenHash)
            players[player.id] = updatedPlayer
            return LoginOutputModel(decode(newTokenHash), player.id)
        }
        return null
    }

    override fun logout(id: Int) {
        val player = players.values.find { it.id == id } ?: return
        players[player.id] = player.copy(tokenHash = null)
    }

    override fun getPlayerHome(token: String): GetPlayerHomeOutputModel? {
        val tokenHash = encode(token)
        return players.values.find { it.tokenHash == tokenHash }?.let {
            GetPlayerHomeOutputModel(it.id, it.name)
        }
    }

    override fun getPlayerIdByToken(token: String): Int? {
        val tokenHash = encode(token)
        return players.entries.find { it.value.tokenHash == tokenHash }?.key
    }

    override fun getPlayerInfo(id: Int): GetPlayerInfoOutputModel? {
        val player = players[id] ?: return null
        return GetPlayerInfoOutputModel(player.id, player.name, player.email)
    }

    override fun searchPlayersByName(name: String, limit: Int, skip: Int): SearchPlayersByNameOutputModel {
        val lowerCaseName = name.lowercase()
        val players = players.values.filter { it.name.lowercase().contains(lowerCaseName) }
        val limitedPlayers = players
            .subList(
                skip,
                if (skip + limit > players.size)
                    players.size else (skip + limit)
            )
        return SearchPlayersByNameOutputModel(limitedPlayers.map { GetPlayerInfoOutputModel(it.id, it.name, it.email) })
    }

    override fun checkIfPlayerExists(name: String): Boolean {
        return players.values.any { it.name == name }
    }

    override fun checkIfPlayerTokenExists(token: String): Boolean {
        return players.values.any {
            it.tokenHash == encode(token)
        }
    }
}
