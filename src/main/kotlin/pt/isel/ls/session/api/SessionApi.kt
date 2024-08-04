package pt.isel.ls.session.api

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
import pt.isel.ls.session.domain.SessionQuery
import pt.isel.ls.session.domain.models.input.SessionCreateInputModel
import pt.isel.ls.session.domain.models.input.SessionUpdateInputModel
import pt.isel.ls.session.services.SessionServices
import pt.isel.ls.utils.InvalidPlayerIdException
import pt.isel.ls.utils.InvalidSessionIdException
import pt.isel.ls.utils.Paging
import pt.isel.ls.utils.exceptionHandler
import pt.isel.ls.utils.getBody
import pt.isel.ls.utils.getOptionals
import pt.isel.ls.utils.getPathParam
import pt.isel.ls.utils.logRequest

class SessionApi(private val services: SessionServices, authHandler: AuthHandler) {

    val routes = routes(
        "/session" bind Method.POST to authHandler.handle.then { exceptionHandler { createSession(it) } },
        "/session/{id}" bind Method.DELETE to authHandler.handle.then { exceptionHandler { deleteSession(it) } },
        "/session/{id}/players" bind Method.PATCH to authHandler.handle.then { exceptionHandler { addPlayerToSession(it) } },
        "/session/{id}/player" bind Method.PATCH to authHandler.handle.then { exceptionHandler { removePlayerFromSession(it) } },
        "/session/info/{id}" bind Method.GET to { exceptionHandler { getSessionInfo(it) } },
        "/session/search" bind Method.GET to { exceptionHandler { getSessions(it) } },
        "/session/{id}" bind Method.PATCH to authHandler.handle.then { exceptionHandler { updateSession(it) } }
    )

    private fun createSession(request: Request): Response {
        logRequest(request)
        val session = getBody<SessionCreateInputModel>(request.bodyString())
        val hostId = request.header("playerId")?.toIntOrNull() ?: throw InvalidPlayerIdException
        val dataToSend = services.createSession(session.date, session.gameId, session.capacity, hostId)
        val resBody = Json.encodeToString(dataToSend)
        return Response(CREATED).header("content-type", "application/json").body(resBody)
    }

    private fun deleteSession(request: Request): Response {
        logRequest(request)
        val idParam = getPathParam(request, "id")
        val id = idParam.toIntOrNull() ?: throw InvalidSessionIdException
        val hostId = request.header("playerId")?.toIntOrNull() ?: throw InvalidPlayerIdException
        services.deleteSession(id, hostId)
        return Response(OK)
    }

    private fun addPlayerToSession(request: Request): Response {
        logRequest(request)
        val playerId = request.header("playerId")?.toIntOrNull() ?: throw InvalidPlayerIdException
        val idParam = getPathParam(request, "id")
        val sessionId = idParam.toIntOrNull() ?: throw InvalidSessionIdException
        services.addPlayerToSession(sessionId, playerId)
        return Response(OK)
    }

    private fun removePlayerFromSession(request: Request): Response {
        logRequest(request)
        val playerId = request.header("playerId")?.toIntOrNull() ?: throw InvalidPlayerIdException
        val idParam = getPathParam(request, "id")
        val sessionId = idParam.toIntOrNull() ?: throw InvalidSessionIdException
        services.removePlayerFromSession(sessionId, playerId)
        return Response(OK)
    }

    private fun getSessionInfo(request: Request): Response {
        logRequest(request)
        val sessionIdParam = getPathParam(request, "id")
        val id = sessionIdParam.toIntOrNull() ?: throw InvalidSessionIdException
        val dataToSend = services.getSessionInfo(id)
        val resBody = Json.encodeToString(dataToSend)
        return Response(OK).header("content-type", "application/json").body(resBody)
    }

    private fun getSessions(request: Request): Response {
        logRequest(request)
        val optionals = getOptionals(request, listOf("gid", "date", "state", "pid"))
        val limit = request.query("limit")?.toIntOrNull() ?: Paging.DEFAULT_LIMIT
        val skip = request.query("skip")?.toIntOrNull() ?: Paging.DEFAULT_SKIP
        val dataToSend = services.getSessions(SessionQuery(skip, limit, optionals))
        val resBody = Json.encodeToString(dataToSend)
        return Response(OK).header("content-type", "application/json").body(resBody)
    }

    private fun updateSession(request: Request): Response {
        logRequest(request)
        val sessionIdParam = getPathParam(request, "id")
        val sessionId = sessionIdParam.toIntOrNull() ?: throw InvalidSessionIdException
        val updatedSession = getBody<SessionUpdateInputModel>(request.bodyString())
        val hostId = request.header("playerId")?.toIntOrNull() ?: throw InvalidPlayerIdException
        services.updateSession(sessionId, hostId, updatedSession)
        return Response(OK)
    }
}
