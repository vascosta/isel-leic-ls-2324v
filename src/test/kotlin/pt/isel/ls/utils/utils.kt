package pt.isel.ls.utils

import org.http4k.core.Method
import org.http4k.core.Request
import pt.isel.ls.player.api.PlayerApi
import pt.isel.ls.player.domain.CreatePlayerOutputModel
import pt.isel.ls.utils.getBody
import java.time.OffsetDateTime

fun createAndGetPlayerToken(api: PlayerApi): CreatePlayerOutputModel {
    val randomName = "player" + (0..10000).random()
    val randomEmail = randomName + "@email.com"
    val request = Request(Method.POST, "/player")
        .header("content-type", "application/json")
        .body(
            """
                    {
                        "name": "$randomName",
                        "email": "$randomEmail",
                        "password": "Password1"
                    } 
                """
        )
    val response = api.routes(request)
    val body = getBody<CreatePlayerOutputModel>(response.bodyString())
    return body
}

fun updateSessionRequest(sessionId: Int, capacity: Int?, gameId: Int?, date: String?, playerToken: String): Request {
    val request = Request(Method.PATCH, "/session/$sessionId")
        .header("content-type", "application/json")
        .header("authorization", "Bearer $playerToken")

    val bodyMap = mutableMapOf<String, Any>()

    capacity?.let { bodyMap["capacity"] = it }
    gameId?.let { bodyMap["gameId"] = it }
    date?.let { bodyMap["date"] = it }

    val jsonBody = bodyMap.entries.joinToString(prefix = "{", postfix = "}") { (key, value) -> "\"$key\":\"$value\"" }

    val jsonBodyWithoutLastComma = jsonBody.removeSuffix(",")

    return request.body(jsonBodyWithoutLastComma)
}

fun deleteSessionRequest(sessionId: Int, hostId: Int, playerToken: String): Request {
    return Request(Method.DELETE, "/session/$sessionId")
        .header("content-type", "application/json")
        .header("authorization", "Bearer $playerToken")
        .header("playerId", "$hostId")
}

fun removePlayerFromSessionRequest(sessionId: Int, playerId: Int, playerToken: String): Request {
    return Request(Method.PATCH, "/session/$sessionId/player")
        .header("content-type", "application/json")
        .header("authorization", "Bearer $playerToken")
        .header("playerId", "$playerId")
}

fun createSessionRequest(capacity: Int, gameId: Int, date: String, playerId: Int, playerToken: String): Request {
    return Request(Method.POST, "/session")
        .header("content-type", "application/json")
        .header("authorization", "Bearer $playerToken")
        .header("playerId", "$playerId")
        .body(
            """
                    {
                        "capacity":$capacity,
                        "date":"$date",
                        "gameId":$gameId
                    }
                """
        )
}

fun addPlayerToSessionRequest(sessionId: Int, playerId: Int, playerToken: String): Request {
    return Request(Method.PATCH, "/session/$sessionId/players")
        .header("content-type", "application/json")
        .header("authorization", "Bearer $playerToken")
        .header("playerId", "$playerId")
}