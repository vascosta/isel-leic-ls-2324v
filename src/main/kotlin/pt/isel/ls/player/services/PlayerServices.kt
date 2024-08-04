package pt.isel.ls.player.services

import pt.isel.ls.player.dataMem.PlayerDataAccess
import pt.isel.ls.player.domain.CreatePlayerOutputModel
import pt.isel.ls.player.domain.GetPlayerHomeOutputModel
import pt.isel.ls.player.domain.GetPlayerInfoOutputModel
import pt.isel.ls.player.domain.LoginOutputModel
import pt.isel.ls.player.domain.SearchPlayersByNameOutputModel
import pt.isel.ls.session.utils.checkPlayerId
import pt.isel.ls.utils.PlayerAlreadyExistsException
import pt.isel.ls.utils.PlayerNotFoundException
import pt.isel.ls.utils.checkEmail
import pt.isel.ls.utils.checkPassword
import pt.isel.ls.utils.checkPlayerName

class PlayerServices(private val dataAccess: PlayerDataAccess) {

    fun createPlayer(name: String, email: String, password: String): CreatePlayerOutputModel {
        checkPlayerName(name)
        checkEmail(email)
        checkPassword(password)
        checkIfPlayerExists(name)
        return dataAccess.createPlayer(name, email, password)
    }

    fun login(name: String, password: String): LoginOutputModel {
        checkPlayerName(name)
        checkPassword(password)
        return dataAccess.login(name, password) ?: throw PlayerNotFoundException
    }

    fun logout(id: Int) {
        checkPlayerId(id)
        dataAccess.logout(id)
    }

    fun getPlayerHome(token: String): GetPlayerHomeOutputModel {
        return dataAccess.getPlayerHome(token) ?: throw PlayerNotFoundException
    }

    fun getPlayerIdByToken(token: String): Int? {
        return dataAccess.getPlayerIdByToken(token)
    }

    fun getPlayerInfo(id: Int): GetPlayerInfoOutputModel {
        checkPlayerId(id)
        return dataAccess.getPlayerInfo(id) ?: throw PlayerNotFoundException
    }

    fun searchPlayersByName(name: String, limit: Int, skip: Int): SearchPlayersByNameOutputModel {
        checkPlayerName(name)
        return dataAccess.searchPlayersByName(name, limit, skip)
    }

    private fun checkIfPlayerExists(name: String) {
        if (dataAccess.checkIfPlayerExists(name)) throw PlayerAlreadyExistsException
    }

    fun checkIfPlayerTokenExists(token: String): Boolean {
        return dataAccess.checkIfPlayerTokenExists(token)
    }
}
