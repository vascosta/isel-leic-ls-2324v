package pt.isel.ls.integration

import org.postgresql.ds.PGSimpleDataSource
import pt.isel.ls.app.api.AppApi
import pt.isel.ls.app.dataMem.AppDataAccess
import pt.isel.ls.app.services.AppServices
import pt.isel.ls.auth.AuthHandler
import pt.isel.ls.player.api.PlayerApi
import pt.isel.ls.utils.createAndGetPlayerToken

open class IntegrationBaseTest protected constructor() {

    companion object {
        private val db = PGSimpleDataSource()
        private val dataBase = AppDataAccess(db)
        private val services = AppServices(dataBase)
        val api = AppApi(services)

        fun clearDataBase() {
            clearSessionData()
            clearGameData()
            clearPlayerData()
        }

        private fun clearGameData() {
            db.getConnection().use {
                val stm = it.prepareStatement("delete from game")
                stm.execute()
            }
        }

        private fun clearSessionData() {
            db.getConnection().use {
                val stm = it.prepareStatement("delete from session")
                stm.execute()
            }
        }

        private fun clearPlayerData() {
            db.getConnection().use {
                val stm = it.prepareStatement("delete from player")
                stm.execute()
            }
        }
    }
}