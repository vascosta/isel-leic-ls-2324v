package pt.isel.ls.session.datamem

import org.postgresql.ds.PGSimpleDataSource
import pt.isel.ls.game.domain.GameIdentificationOutputModel
import pt.isel.ls.session.domain.SessionQuery
import pt.isel.ls.session.domain.models.input.SessionUpdateInputModel
import pt.isel.ls.session.domain.models.output.SessionDetailsOutputModel
import pt.isel.ls.session.domain.models.output.SessionIdentifierOutputModel
import pt.isel.ls.session.domain.models.output.SessionPlayerOutputModel
import pt.isel.ls.utils.getArray
import java.time.OffsetDateTime
import java.time.ZoneOffset

class SessionDataBase(private val db: PGSimpleDataSource) : SessionDataAccess {

    override fun createSession(capacity: Int, gameId: Int, date: OffsetDateTime, hostId: Int): SessionIdentifierOutputModel {
        db.connection.use {
            val stm = it.prepareStatement(
                "insert into session(capacity, date, game, players, host) " +
                    "values (?, date_trunc('minute', ?::timestamp), ?, array[?], ?) returning id;"
            )
            stm.setInt(1, capacity)
            stm.setObject(2, date)
            stm.setInt(3, gameId)
            stm.setInt(4, hostId)
            stm.setInt(5, hostId)
            val rs = stm.executeQuery()
            rs.next()
            return SessionIdentifierOutputModel(rs.getInt("id"))
        }
    }

    override fun deleteSession(sessionId: Int): Boolean {
        db.connection.use {
            val stm = it.prepareStatement("delete from session where id = ?;")
            stm.setInt(1, sessionId)
            val affectedRows = stm.executeUpdate()
            return affectedRows > 0
        }
    }

    override fun addPlayerToSession(sessionId: Int, playerId: Int): Boolean {
        db.connection.use {
            val stm = it.prepareStatement("update session set players = array_append(players, ?) where id = ?;")
            stm.setInt(1, playerId)
            stm.setInt(2, sessionId)
            val rs = stm.executeUpdate()
            return rs != 0
        }
    }

    override fun removePlayerFromSession(sessionId: Int, playerId: Int): Boolean {
        db.connection.use {
            val stm = it.prepareStatement("update session set players = array_remove(players, ?) where id = ?;")
            stm.setInt(1, playerId)
            stm.setInt(2, sessionId)
            val rs = stm.executeUpdate()
            return rs != 0
        }
    }

    override fun getSessionInfo(sessionId: Int): SessionDetailsOutputModel? {
        db.connection.use {
            val stm = it.prepareStatement(
                "SELECT s.id, s.capacity, s.date, s.game, s.players, s.host, g.name AS game_name " +
                    "FROM session s " +
                    "LEFT JOIN game g ON s.game = g.id " +
                    "WHERE s.id = ?;"
            )
            stm.setInt(1, sessionId)
            val rs = stm.executeQuery()
            if (!rs.next()) return null
            val playersArray = rs.getArray("players")
            val playersList = getPlayersList(playersArray)
            val game = GameIdentificationOutputModel(rs.getInt("game"), rs.getString("game_name"))
            return SessionDetailsOutputModel(
                rs.getInt("id"),
                rs.getInt("capacity"),
                rs.getString("date"),
                game,
                playersList,
                rs.getInt("host")
            )
        }
    }

    override fun getSessions(sessionQuery: SessionQuery): List<SessionDetailsOutputModel> {
        val sessions = mutableListOf<SessionDetailsOutputModel>()
        db.connection.use { connection ->
            val query = "SELECT s.*, g.id AS game_id, g.name AS game_name FROM session s " +
                "LEFT JOIN game g ON s.game = g.id " +
                if (sessionQuery.optionals.isNotEmpty()) {
                    " WHERE ${buildQueryFromOptionals(sessionQuery.optionals).joinToString(" AND ")} ORDER BY s.id"
                } else {
                    " ORDER BY s.id"
                }
            val queryAndPaging = "$query LIMIT ${sessionQuery.limit} OFFSET ${sessionQuery.skip}"

            val stm = connection.prepareStatement(queryAndPaging)
            sessionQuery.optionals.values.forEachIndexed { index, value -> stm.setString(index + 1, value) }

            val rs = stm.executeQuery()
            while (rs.next()) {
                val id = rs.getInt("id")
                val capacity = rs.getInt("capacity")
                val timestamp = rs.getTimestamp("date").toInstant()
                val offsetDateTime = OffsetDateTime.ofInstant(timestamp, ZoneOffset.UTC).toString()
                val gameId = rs.getInt("game_id")
                val gameName = rs.getString("game_name")
                val game = GameIdentificationOutputModel(gameId, gameName)
                val playersArray = rs.getArray("players")
                val playersList = getPlayersList(playersArray)
                val hostId = rs.getInt("host")
                sessions.add(SessionDetailsOutputModel(id, capacity, offsetDateTime, game, playersList, hostId))
            }
        }
        return sessions
    }

    override fun updateSession(sessionId: Int, updatedSession: SessionUpdateInputModel) {
        db.connection.use {
            val stm = it.prepareStatement(
                """
            update session 
            set capacity = COALESCE(?, capacity), 
                date = COALESCE(date_trunc('minute', ?::timestamp), date), 
                game = COALESCE(?, game) 
            where id = ?;
                """.trimIndent()
            )
            stm.setObject(1, updatedSession.capacity)
            stm.setObject(2, updatedSession.date)
            stm.setObject(3, updatedSession.gameId)
            stm.setInt(4, sessionId)
            stm.executeUpdate()
        }
    }

    override fun isSessionFull(sessionId: Int): Boolean {
        db.connection.use {
            val stm = it.prepareStatement(
                "select case when array_length(players, 1) >= capacity then true else false " +
                    "end as isfull from session where id = ?;"
            )
            stm.setInt(1, sessionId)
            val rs = stm.executeQuery()
            rs.next()
            return rs.getBoolean("isfull")
        }
    }

    override fun isSessionHost(sessionId: Int, playerId: Int): Boolean {
        db.connection.use {
            val stm = it.prepareStatement("select * from session where id = ? and host = ?;")
            stm.setInt(1, sessionId)
            stm.setInt(2, playerId)
            val rs = stm.executeQuery()
            return rs.next()
        }
    }

    override fun playerInSession(sessionId: Int, playerId: Int): Boolean {
        db.connection.use {
            val stm = it.prepareStatement("select case when ? = any(players) then true else false end as isin from session where id = ?;")
            stm.setInt(1, playerId)
            stm.setInt(2, sessionId)
            val rs = stm.executeQuery()
            rs.next()
            return rs.getBoolean("isin")
        }
    }

    override fun sessionExists(sessionId: Int): Boolean {
        db.connection.use {
            val stm = it.prepareStatement("select * from session where id = ?;")
            stm.setInt(1, sessionId)
            return stm.executeQuery().next()
        }
    }

    override fun isSessionExpired(sessionId: Int): Boolean {
        db.connection.use {
            val stm = it.prepareStatement("select * from session where id = ? and date < CURRENT_TIMESTAMP;")
            stm.setInt(1, sessionId)
            return stm.executeQuery().next()
        }
    }

    override fun isValidCapacity(sessionId: Int, capacity: Int?): Boolean {
        if (capacity == null) return true
        db.connection.use {
            val stm = it.prepareStatement("select players from session where id = ?;")
            stm.setInt(1, sessionId)
            val rs = stm.executeQuery()
            if (rs.next()) {
                val players = getArray<Int>(rs.getArray("players"))
                return players.size <= capacity
            }
            return false
        }
    }

    private fun getPlayersList(playersArray: java.sql.Array): List<SessionPlayerOutputModel> {
        val playerIds = getArray<Int>(playersArray)
        val players = mutableListOf<SessionPlayerOutputModel>()
        playerIds.forEach { playerId ->
            val player = getPlayerInfo(playerId)
            player?.let { players.add(it) }
        }
        return players
    }

    private fun getPlayerInfo(playerId: Int): SessionPlayerOutputModel? {
        db.connection.use {
            val stm = it.prepareStatement("select id, name from player where id = ?;")
            stm.setInt(1, playerId)
            val rs = stm.executeQuery()
            return if (rs.next()) {
                SessionPlayerOutputModel(
                    rs.getInt("id"),
                    rs.getString("name")
                )
            } else {
                null
            }
        }
    }

    private fun buildQueryFromOptionals(optionals: HashMap<String, String>): List<String> {
        val queryParams = mutableListOf<String>()
        optionals.forEach { (key, _) ->
            when (key) {
                "gid" -> queryParams.add("game = ?::integer")
                "pid" -> queryParams.add("players @> ARRAY[?]::int[]")
                "date" -> queryParams.add("date_trunc('day', \"date\") = date_trunc('day', ?::timestamp)")
                "state" -> queryParams.add("?::boolean = (cardinality(players) = capacity)")
            }
        }
        return queryParams.toList()
    }
}
