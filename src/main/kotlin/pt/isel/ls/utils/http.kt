package pt.isel.ls.utils

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.routing.path
import org.slf4j.LoggerFactory
import pt.isel.ls.app.domain.ApiException
import pt.isel.ls.game.domain.GameFiltersInputModel
import pt.isel.ls.game.domain.Genres

val logger = LoggerFactory.getLogger("pt.isel.ls.sessionsServer")

fun getSearchQuery(request: Request): GameFiltersInputModel {
    val queryGenres = request.query("genres")
    val genres = queryGenres?.takeIf { it.isNotEmpty() }?.split(",")?.map { Genres.toGenre(it) }
    val developer = request.query("developer")?.takeIf { it.isNotEmpty() }
    val name = request.query("name")?.takeIf { it.isNotEmpty() }

    return GameFiltersInputModel(genres, developer, name)
}

inline fun <reified T> getBody(encodedBody: String): T {
    try {
        return Json.decodeFromString<T>(encodedBody)
    } catch (e: Exception) {
        throw InvalidBodyInputException
    }
}

fun getPathParam(request: Request, paramName: String): String {
    return request.path(paramName) ?: throw Exception("Missing $paramName parameter")
}

fun getOptionals(request: Request, optionals: List<String>): HashMap<String, String> {
    val optionalsMap = hashMapOf<String, String>()
    optionals.forEach {
        val value = request.query(it)
        if (value != null) {
            optionalsMap[it] = value
        }
    }
    return optionalsMap
}

fun exceptionHandler(apiHandler: () -> Response): Response {
    return try {
        apiHandler()
    } catch (e: Exception) {
        val status = when (e) {
            in ApiException.NOT_FOUND -> Status.NOT_FOUND
            in ApiException.BAD_REQUEST -> Status.BAD_REQUEST
            in ApiException.FORBIDDEN -> Status.FORBIDDEN
            in ApiException.UNAUTHORIZED -> Status.UNAUTHORIZED
            else -> Status.INTERNAL_SERVER_ERROR
        }
        val apiException = ApiException(e.message ?: "Internal Server Error")
        Response(status).body(Json.encodeToString(apiException)).header("content-type", "application/json")
    }
}

fun logRequest(request: Request) {
//    logger.info(
//        "incoming request: method={}, uri={}, content-type={} accept={}",
//        request.method,
//        request.uri,
//        request.header("content-type"),
//        request.header("accept"),
//    )
}
