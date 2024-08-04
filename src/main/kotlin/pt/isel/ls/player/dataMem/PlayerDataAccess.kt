package pt.isel.ls.player.dataMem

import pt.isel.ls.player.domain.CreatePlayerOutputModel
import pt.isel.ls.player.domain.GetPlayerHomeOutputModel
import pt.isel.ls.player.domain.GetPlayerInfoOutputModel
import pt.isel.ls.player.domain.LoginOutputModel
import pt.isel.ls.player.domain.SearchPlayersByNameOutputModel

interface PlayerDataAccess {
    fun createPlayer(name: String, email: String, password: String): CreatePlayerOutputModel

    fun login(name: String, password: String): LoginOutputModel?
    fun logout(id: Int)

    fun getPlayerHome(token: String): GetPlayerHomeOutputModel?
    fun getPlayerIdByToken(token: String): Int?
    fun getPlayerInfo(id: Int): GetPlayerInfoOutputModel?

    fun searchPlayersByName(name: String, limit: Int, skip: Int): SearchPlayersByNameOutputModel

    fun checkIfPlayerExists(name: String): Boolean
    fun checkIfPlayerTokenExists(token: String): Boolean
}
