package pt.isel.ls.app.dataMem

import org.postgresql.ds.PGSimpleDataSource
import pt.isel.ls.game.dataMem.GameDataBase
import pt.isel.ls.game.dataMem.GameDataMem
import pt.isel.ls.player.dataMem.PlayerDataBase
import pt.isel.ls.player.dataMem.PlayerDataMem
import pt.isel.ls.session.datamem.SessionDataBase
import pt.isel.ls.session.datamem.SessionDataMem

class AppDataAccess(db: PGSimpleDataSource? = null) {

    val playerDataAccess = if (db != null) PlayerDataBase(db) else PlayerDataMem()
    val gameDataAccess = if (db != null) GameDataBase(db) else GameDataMem()
    val sessionDataAccess = if (db != null) SessionDataBase(db) else SessionDataMem()

    init {
        if (db != null) {
            val jdbcUrl = System.getenv("JDBC_DATABASE_URL")
            db.setUrl(jdbcUrl)
        }
    }
}
