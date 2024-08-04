package pt.isel.ls.player.api

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.CREATED
import org.http4k.core.Status.Companion.OK
import org.http4k.core.then
import org.http4k.routing.bind
import org.http4k.routing.routes
import pt.isel.ls.auth.AuthHandler
import pt.isel.ls.player.domain.CreatePlayerInputModel
import pt.isel.ls.player.domain.LoginInputModel
import pt.isel.ls.player.services.PlayerServices
import pt.isel.ls.utils.InvalidPlayerIdException
import pt.isel.ls.utils.Paging
import pt.isel.ls.utils.exceptionHandler
import pt.isel.ls.utils.getBody
import pt.isel.ls.utils.getPathParam
import pt.isel.ls.utils.logRequest

class PlayerApi(private val services: PlayerServices, authHandler: AuthHandler) {

    val routes = routes(
        "/home/{token}" bind Method.GET to { req -> exceptionHandler { getPlayerHome(req) } },
        "/login" bind Method.PATCH to { req -> exceptionHandler { login(req) } },
        "/logout" bind Method.PATCH to authHandler.handle.then { req -> exceptionHandler { logout(req) } },
        "/player" bind Method.POST to { req -> exceptionHandler { createPlayer(req) } },
        "/player/{id}" bind Method.GET to authHandler.handle.then { req -> exceptionHandler { getPlayerInfo(req) } },
        "/players/search" bind Method.GET to { req -> exceptionHandler { searchPlayersByName(req) } }
    )

    fun createPlayer(request: Request): Response {
        logRequest(request)
        val params = getBody<CreatePlayerInputModel>(request.bodyString())
        val dataToSend = services.createPlayer(params.name, params.email, params.password)
        val resBody = Json.encodeToString(dataToSend)
        return Response(CREATED)
            .header("content-type", "application/json")
            .header("token", dataToSend.token)
            .body(resBody)
    }

    fun login(request: Request): Response {
        logRequest(request)
        val params = getBody<LoginInputModel>(request.bodyString())
        val dataToSend = services.login(params.name, params.password)
        val resBody = Json.encodeToString(dataToSend)
        return Response(OK)
            .header("content-type", "application/json")
            .body(resBody)
    }

    fun logout(request: Request): Response {
        logRequest(request)
        val playerId = request.header("playerId")?.toIntOrNull() ?: throw InvalidPlayerIdException
        services.logout(playerId)
        return Response(OK)
    }

    fun getPlayerHome(request: Request): Response {
        logRequest(request)
        val token = getPathParam(request, "token")
        val dataToSend = services.getPlayerHome(token)
        val resBody = Json.encodeToString(dataToSend)
        return Response(OK)
            .header("content-type", "application/json")
            .body(resBody)
    }

    fun getPlayerInfo(request: Request): Response {
        logRequest(request)
        val idParam = getPathParam(request, "id")
        val id = idParam.toIntOrNull() ?: throw InvalidPlayerIdException
        val dataToSend = services.getPlayerInfo(id)
        val resBody = Json.encodeToString(dataToSend)
        return Response(OK)
            .header("content-type", "application/json")
            .body(resBody)
    }

    fun searchPlayersByName(request: Request): Response {
        logRequest(request)
        val name = request.query("name") ?: throw Exception("Missing name parameter")
        val limit = request.query("limit")?.toIntOrNull() ?: Paging.DEFAULT_LIMIT
        val skip = request.query("skip")?.toIntOrNull() ?: Paging.DEFAULT_SKIP
        val dataToSend = services.searchPlayersByName(name, limit, skip)
        val resBody = Json.encodeToString(dataToSend)
        return Response(OK)
            .header("content-type", "application/json")
            .body(resBody)
    }
}
