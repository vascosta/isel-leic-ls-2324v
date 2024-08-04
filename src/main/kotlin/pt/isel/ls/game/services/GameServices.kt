package pt.isel.ls.game.services

import pt.isel.ls.game.dataMem.GameDataAccess
import pt.isel.ls.game.domain.GameIdOutputModel
import pt.isel.ls.game.domain.GameListOutputModel
import pt.isel.ls.game.domain.GameOutputModel
import pt.isel.ls.game.domain.GameSearchQuery
import pt.isel.ls.game.domain.Genres
import pt.isel.ls.game.domain.toOriginalName
import pt.isel.ls.session.utils.checkGameId
import pt.isel.ls.utils.GameAlreadyExistsException
import pt.isel.ls.utils.GameNotFoundException
import pt.isel.ls.utils.checkDeveloper
import pt.isel.ls.utils.checkGameName
import pt.isel.ls.utils.checkQueryDeveloper
import pt.isel.ls.utils.checkQueryName

class GameServices(private val data: GameDataAccess) {

    fun createGame(name: String, developer: String, genres: List<Genres>): GameIdOutputModel {
        checkGameName(name)
        checkDeveloper(developer)
        checkIfGameExists(name)
        return data.createGame(name, developer, genres)
    }

    fun getGameDetails(id: Int): GameOutputModel {
        checkGameId(id)
        return data.getGameDetails(id) ?: throw GameNotFoundException
    }

    fun getGames(gameQuery: GameSearchQuery): GameListOutputModel {
        val name = gameQuery.name
        val developer = gameQuery.developer
        checkQueryName(name)
        checkQueryDeveloper(developer)
        val originalName = name?.toOriginalName()
        val originalDeveloper = developer?.toOriginalName()
        if (originalName != null) {
            checkGameName(originalName)
        }
        if (originalDeveloper != null) {
            checkDeveloper(originalDeveloper)
        }
        return data.getGames(GameSearchQuery(gameQuery.skip, gameQuery.limit, gameQuery.genres, originalDeveloper, originalName))
    }

    private fun checkIfGameExists(name: String) {
        if (data.checkIfGameExists(name)) throw GameAlreadyExistsException
    }
}
