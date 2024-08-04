package pt.isel.ls.player.dataMem

import org.postgresql.ds.PGSimpleDataSource
import pt.isel.ls.player.domain.CreatePlayerOutputModel
import pt.isel.ls.player.domain.GetPlayerHomeOutputModel
import pt.isel.ls.player.domain.GetPlayerInfoOutputModel
import pt.isel.ls.player.domain.LoginOutputModel
import pt.isel.ls.player.domain.SearchPlayersByNameOutputModel
import pt.isel.ls.utils.decode
import pt.isel.ls.utils.encode
import java.sql.Statement
import java.util.UUID

class PlayerDataBase(private val db: PGSimpleDataSource) : PlayerDataAccess {

    override fun createPlayer(name: String, email: String, password: String): CreatePlayerOutputModel {
        val token = UUID.randomUUID().toString()
        val tokenHash = encode(token)
        val passwordHash = encode(password)
        db.getConnection().use {
            val stm = it.prepareStatement(
                "insert into Player(name, email, passwordHash, tokenHash) values('$name', '$email', '$passwordHash', '$tokenHash')",
                Statement.RETURN_GENERATED_KEYS
            )
            stm.execute()
            val rs = stm.getGeneratedKeys()
            rs.next()
            val id = rs.getInt("id")
            return CreatePlayerOutputModel(token, id)
        }
    }

    override fun login(name: String, password: String): LoginOutputModel? {
        db.getConnection().use {
            val stm = it.prepareStatement("select id, passwordHash, tokenHash from player where name = '$name'")
            val rs = stm.executeQuery()
            if (rs.next()) {
                val pass = decode(rs.getString("passwordhash"))
                if (pass == password) {
                    val newTokenHash = encode(UUID.randomUUID().toString())
                    val updateStm = it.prepareStatement("update player set tokenHash = '$newTokenHash' where name = '$name'")
                    updateStm.execute()
                    return LoginOutputModel(
                        decode(newTokenHash),
                        rs.getInt("id")
                    )
                }
            }
        }
        return null
    }

    override fun logout(id: Int) {
        db.getConnection().use {
            val stm = it.prepareStatement("update player set tokenHash = null where id = $id")
            stm.execute()
        }
    }

    override fun getPlayerHome(token: String): GetPlayerHomeOutputModel? {
        db.getConnection().use {
            val tokenHash = encode(token)
            val stm = it.prepareStatement("select id, name from player where tokenHash = '$tokenHash'")
            val rs = stm.executeQuery()
            if (rs.next()) {
                return GetPlayerHomeOutputModel(rs.getInt("id"), rs.getString("name"))
            }
        }
        return null
    }

    override fun getPlayerIdByToken(token: String): Int? {
        db.getConnection().use {
            val stm = it.prepareStatement("select id from player where tokenHash = '${encode(token)}'")
            val rs = stm.executeQuery()
            if (rs.next()) {
                return rs.getInt("id")
            }
        }
        return null
    }

    override fun getPlayerInfo(id: Int): GetPlayerInfoOutputModel? {
        db.getConnection().use {
            val stm = it.prepareStatement("select id, name, email from player where id = $id")
            val rs = stm.executeQuery()
            if (rs.next()) {
                return GetPlayerInfoOutputModel(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email")
                )
            }
        }
        return null
    }

    override fun searchPlayersByName(name: String, limit: Int, skip: Int): SearchPlayersByNameOutputModel {
        db.getConnection().use {
            val stm = it.prepareStatement(
                "select id, name, email from player " +
                    "where lower(name) like lower(?) order by name limit $limit offset $skip"
            )
            stm.setString(1, "%$name%")
            val rs = stm.executeQuery()
            val players = mutableListOf<GetPlayerInfoOutputModel>()

            while (rs.next()) {
                players.add(
                    GetPlayerInfoOutputModel(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email")
                    )
                )
            }
            return SearchPlayersByNameOutputModel(players)
        }
    }

    override fun checkIfPlayerExists(name: String): Boolean {
        db.getConnection().use {
            val stm = it.prepareStatement("select * from player where name = '$name'")
            val rs = stm.executeQuery()
            return rs.next()
        }
    }

    override fun checkIfPlayerTokenExists(token: String): Boolean {
        db.getConnection().use {
            val stm = it.prepareStatement("select * from player where tokenHash = '${encode(token)}'")
            val rs = stm.executeQuery()
            return rs.next()
        }
    }
}
