package pt.isel.ls.app.services

import pt.isel.ls.app.dataMem.AppDataAccess
import pt.isel.ls.game.services.GameServices
import pt.isel.ls.player.services.PlayerServices
import pt.isel.ls.session.services.SessionServices

class AppServices(dataAccess: AppDataAccess) {

    val playerServices = PlayerServices(dataAccess.playerDataAccess)
    val gameServices = GameServices(dataAccess.gameDataAccess)
    val sessionServices = SessionServices(dataAccess.sessionDataAccess)
}
