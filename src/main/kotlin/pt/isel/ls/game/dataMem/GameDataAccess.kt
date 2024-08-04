package pt.isel.ls.game.dataMem

import pt.isel.ls.game.domain.GameIdOutputModel
import pt.isel.ls.game.domain.GameListOutputModel
import pt.isel.ls.game.domain.GameOutputModel
import pt.isel.ls.game.domain.GameSearchQuery
import pt.isel.ls.game.domain.Genres

interface GameDataAccess {
    fun createGame(name: String, developer: String, genres: List<Genres>): GameIdOutputModel
    fun getGameDetails(id: Int): GameOutputModel?
    fun getGames(gameQuery: GameSearchQuery): GameListOutputModel
    fun checkIfGameExists(name: String): Boolean
}
