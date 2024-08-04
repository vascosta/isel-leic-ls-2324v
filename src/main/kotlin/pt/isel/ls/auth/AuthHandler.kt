package pt.isel.ls.auth

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.http4k.core.Filter
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import pt.isel.ls.app.domain.ApiException
import pt.isel.ls.player.services.PlayerServices
import pt.isel.ls.utils.PlayerNotLoggedInException

class AuthHandler(private val services: PlayerServices) {

    val handle: Filter = Filter { next ->
        { request ->
            val playerId = authenticate(request)
            if (playerId != null) {
                next(request.header("playerId", playerId.toString()))
            } else {
                val exception = ApiException(PlayerNotLoggedInException.message.toString())
                Response(Status.UNAUTHORIZED).body(Json.encodeToString(exception))
            }
        }
    }

    fun authenticate(request: Request): Int? {
        val token = getBearerToken(request) ?: return null
        if (!services.checkIfPlayerTokenExists(token)) return null
        return services.getPlayerIdByToken(token)
    }

    private fun getBearerToken(request: Request): String? {
        val authHeader = request.header("Authorization") ?: return null
        return authHeader.split(" ").let {
            if (it.size != 2 || it[0].uppercase() != "BEARER") null
            else it[1]
        }
    }
}
