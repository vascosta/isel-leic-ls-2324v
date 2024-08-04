package pt.isel.ls.unit.session

import pt.isel.ls.session.datamem.SessionDataMem
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

open class SessionBaseTest protected constructor() {

    val sessionDataMem = SessionDataMem()
    val sessionIds = mutableListOf<Int>()

    companion object {
        const val CAPACITY = 10
        const val GAMED = 5
        const val PLAYER = 2
        val DATE_NOW: OffsetDateTime = OffsetDateTime.now()
        val DATE: String = DATE_NOW.plusHours(1).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        const val HOST = 1
        const val GUEST = 40
    }
}
