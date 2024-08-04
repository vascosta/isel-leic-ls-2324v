package pt.isel.ls.game.dataMem

import org.postgresql.ds.PGSimpleDataSource
import pt.isel.ls.game.domain.GameIdOutputModel
import pt.isel.ls.game.domain.GameListOutputModel
import pt.isel.ls.game.domain.GameOutputModel
import pt.isel.ls.game.domain.GameSearchQuery
import pt.isel.ls.game.domain.Genres
import pt.isel.ls.utils.getArray
import java.sql.ResultSet
import java.sql.Statement

class GameDataBase(val db: PGSimpleDataSource) : GameDataAccess {

    override fun createGame(name: String, developer: String, genres: List<Genres>): GameIdOutputModel {
        val genresId = genres.map { Genres.entries.indexOf(it) }
        db.getConnection().use {
            val arg = it.createArrayOf("INTEGER", genresId.toTypedArray())
            val stm = it.prepareStatement(
                "insert into game(name, developer, genres) values(?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            )
            stm.setString(1, name)
            stm.setString(2, developer)
            stm.setArray(3, arg)
            stm.execute()
            val rs = stm.generatedKeys
            rs.next()
            return GameIdOutputModel(rs.getInt("id"))
        }
    }

    override fun getGameDetails(id: Int): GameOutputModel? {
        db.getConnection().use {
            val stm = it.prepareStatement("select * from game where id = $id;")
            val rs = stm.executeQuery()

            if (!rs.next()) return null

            return GameOutputModel(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("developer"),
                getGenresList(rs)
            )
        }
    }

    override fun getGames(gameQuery: GameSearchQuery): GameListOutputModel {
        db.getConnection().use {
            val query = getGamesDBQuery(gameQuery)
            val stm = it.prepareStatement(query)

            if (gameQuery.genres != null) {
                val genresId = gameQuery.genres.map { Genres.entries.indexOf(it) }
                genresId.forEachIndexed { index, genreId ->
                    stm.setInt(index + 1, genreId)
                }
            }

            val rs = stm.executeQuery()

            val games = ArrayList<GameOutputModel>()

            while (rs.next()) {
                games.add(
                    GameOutputModel(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("developer"),
                        getGenresList(rs)
                    )
                )
            }

            return getLimitedArray(games.toList(), gameQuery.skip, gameQuery.limit)
        }
    }

    override fun checkIfGameExists(name: String): Boolean {
        val storedName = name.lowercase()
        db.getConnection().use {
            val stm = it.prepareStatement("select * from game;")
            val rs = stm.executeQuery()

            val gameNames = ArrayList<String>()

            while (rs.next()) {
                gameNames.add(rs.getString("name"))
            }

            return gameNames.map { name -> name.lowercase() }.contains(storedName)
        }
    }

    private fun getGenresList(rs: ResultSet): List<Genres> {
        val genresIds = getArray<Int>(rs.getArray("genres"))
        return genresIds.map { id -> Genres.entries[id] }
    }

    private fun getLimitedArray(games: List<GameOutputModel>, skip: Int, limit: Int): GameListOutputModel {
        return GameListOutputModel(
            games.subList(
                skip,
                if (skip + limit > games.size)
                    games.size else (skip + limit)
            )
        )
    }

    private fun getGamesDBQuery(gameQuery: GameSearchQuery): String {
        val name = gameQuery.name
        val genres = gameQuery.genres
        val developer = gameQuery.developer

        val genresId = genres?.map { Genres.entries.indexOf(it) }
        val genresConditions = genresId?.joinToString(" and ") { "genres @> ARRAY[?]::integer[]" }

        val query = StringBuilder("select * from game where ")

        if (name == null && genres == null && developer == null) {
            return "select * from game"
        }

        if (name != null) {
            query.append("lower(name) like lower('%$name%')")
        }

        if (genres != null) {
            if (query.toString() != "select * from game where ") {
                query.append(" and ")
            }
            query.append(genresConditions)
        }

        if (developer != null) {
            if (query.toString() != "select * from game where ") {
                query.append(" and ")
            }
            query.append("lower(developer) like lower('%$developer%')")
        }

        return query.toString()
    }
}
