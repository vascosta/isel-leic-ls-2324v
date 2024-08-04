package pt.isel.ls.session.utils

import pt.isel.ls.utils.InvalidCapacityException
import pt.isel.ls.utils.InvalidDateException
import pt.isel.ls.utils.InvalidGameIdException
import pt.isel.ls.utils.InvalidPlayerIdException
import pt.isel.ls.utils.InvalidSessionIdException
import pt.isel.ls.utils.InvalidStateException
import pt.isel.ls.utils.checkId
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

fun checkDate(date: OffsetDateTime) {
    if (date.year <= 0 || date.monthValue !in 1..12 || date.dayOfMonth !in 1..31 ||
        date.hour !in 0..23 || date.minute !in 0..59
    ) throw InvalidDateException
    if (OffsetDateTime.now().isAfter(date)) throw InvalidDateException
}
fun checkGameId(gameId: Int) = checkId(gameId, InvalidGameIdException)

fun checkCapacity(capacity: Int) = checkId(capacity, InvalidCapacityException)

fun checkSessionId(sessionId: Int) = checkId(sessionId, InvalidSessionIdException)

fun checkPlayerId(playerId: Int) = checkId(playerId, InvalidPlayerIdException)

fun checkOptionals(optionals: HashMap<String, String>) {
    optionals.forEach { (key, value) ->
        when (key) {
            "gid" -> if (value.toIntOrNull() != null) checkGameId(value.toInt())
            "pid" -> if (value.toIntOrNull() != null) checkPlayerId(value.toInt())
            "date" -> {
                val localDate = LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE)
                val offsetDateTime = localDate.atStartOfDay().atOffset(ZoneOffset.UTC)
                checkDate(offsetDateTime)
            }
            "state" -> {
                if (value != "open" && value != "close") throw InvalidStateException
                optionals[key] = if (value == "open") "false" else "true"
            }
        }
    }
}
