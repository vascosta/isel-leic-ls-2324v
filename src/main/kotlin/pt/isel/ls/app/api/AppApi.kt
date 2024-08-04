package pt.isel.ls.app.api

import org.http4k.routing.ResourceLoader
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.routing.singlePageApp
import pt.isel.ls.app.services.AppServices
import pt.isel.ls.auth.AuthHandler
import pt.isel.ls.game.api.GameApi
import pt.isel.ls.player.api.PlayerApi
import pt.isel.ls.session.api.SessionApi

class AppApi(services: AppServices) {

    val authHandler = AuthHandler(services.playerServices)
    val playerApi = PlayerApi(services.playerServices, authHandler)
    val gameApi = GameApi(services.gameServices, authHandler)
    val sessionApi = SessionApi(services.sessionServices, authHandler)

    val routes = routes(
        "/api" bind routes(
            playerApi.routes,
            gameApi.routes,
            sessionApi.routes
        ),
        singlePageApp(ResourceLoader.Directory("static-content"))
    )
}
