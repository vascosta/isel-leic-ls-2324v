package pt.isel.ls.game.dataMem

import pt.isel.ls.game.domain.GameIdOutputModel
import pt.isel.ls.game.domain.GameListOutputModel
import pt.isel.ls.game.domain.GameOutputModel
import pt.isel.ls.game.domain.GameSearchQuery
import pt.isel.ls.game.domain.Genres
import java.util.concurrent.ConcurrentHashMap

class GameDataMem : GameDataAccess {

    private val games = ConcurrentHashMap<Int, GameOutputModel>()

    override fun createGame(name: String, developer: String, genres: List<Genres>): GameIdOutputModel {
        val id = games.size + 1
        games[id] = GameOutputModel(id, name, developer, genres)
        return GameIdOutputModel(id)
    }

    override fun getGameDetails(id: Int): GameOutputModel? {
        val game = games[id] ?: return null
        return game
    }

    override fun getGames(gameQuery: GameSearchQuery): GameListOutputModel {
        val name = gameQuery.name?.lowercase()
        val genres = gameQuery.genres
        val developer = gameQuery.developer?.lowercase()

        val filteredGames = games.values.filter { game ->
            (name == null || game.name.lowercase().contains(name)) &&
                (genres == null || game.genres.containsAll(genres)) &&
                (developer == null || game.developer.lowercase() == developer)
        }

        val limitedGames = filteredGames
            .drop(gameQuery.skip)
            .take(gameQuery.limit)

        return GameListOutputModel(limitedGames)
    }

    override fun checkIfGameExists(name: String): Boolean {
        val nameStored = name.lowercase()
        val names = games.values.map { it.name.lowercase() }
        return names.contains(nameStored)
    }
}
