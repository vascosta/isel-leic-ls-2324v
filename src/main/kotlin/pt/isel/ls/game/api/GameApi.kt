package pt.isel.ls.game.api

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.CREATED
import org.http4k.core.Status.Companion.OK
import org.http4k.core.then
import org.http4k.routing.bind
import org.http4k.routing.routes
import pt.isel.ls.auth.AuthHandler
import pt.isel.ls.game.domain.CreateGameInputModel
import pt.isel.ls.game.domain.GameSearchQuery
import pt.isel.ls.game.services.GameServices
import pt.isel.ls.utils.InvalidGameIdException
import pt.isel.ls.utils.Paging
import pt.isel.ls.utils.exceptionHandler
import pt.isel.ls.utils.getBody
import pt.isel.ls.utils.getPathParam
import pt.isel.ls.utils.getSearchQuery
import pt.isel.ls.utils.logRequest

class GameApi(private val services: GameServices, authHandler: AuthHandler) {

    val routes = routes(
        "/game" bind POST to authHandler.handle.then { req -> exceptionHandler { createGame(req) } },
        "/game/{id}" bind GET to { req -> exceptionHandler { getGameDetails(req) } },
        "/games" bind GET to { req -> exceptionHandler { getSearchedGames(req) } }
    )

    private fun createGame(request: Request): Response {
        logRequest(request)
        val game = getBody<CreateGameInputModel>(request.bodyString())
        val data = services.createGame(game.name, game.developer, game.genres)
        val rsp = Json.encodeToString(data)
        return Response(CREATED)
            .header("content-type", "application/json")
            .body(rsp)
    }

    private fun getGameDetails(request: Request): Response {
        logRequest(request)
        val gameId = getPathParam(request, "id")
        val id = gameId.toIntOrNull() ?: throw InvalidGameIdException
        val data = services.getGameDetails(id)
        val rsp = Json.encodeToString(data)
        return Response(OK)
            .header("content-type", "application/json")
            .body(rsp)
    }

    private fun getSearchedGames(request: Request): Response {
        logRequest(request)
        val games = getSearchQuery(request)
        val limit = request.query("limit")?.toIntOrNull() ?: Paging.DEFAULT_LIMIT
        val skip = request.query("skip")?.toIntOrNull() ?: Paging.DEFAULT_SKIP
        val data = services.getGames(GameSearchQuery(skip, limit, games.genres, games.developer, games.name))
        val rsp = Json.encodeToString(data)
        return Response(OK)
            .header("content-type", "application/json")
            .body(rsp)
    }
}
