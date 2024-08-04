package pt.isel.ls

import org.http4k.server.Jetty
import org.http4k.server.asServer
import org.postgresql.ds.PGSimpleDataSource
import pt.isel.ls.app.api.AppApi
import pt.isel.ls.app.dataMem.AppDataAccess
import pt.isel.ls.app.services.AppServices
import pt.isel.ls.utils.logger

fun main() {
    val port = System.getenv("PORT").toInt()
    val db = PGSimpleDataSource()
    val dataBase = AppDataAccess(db)
    val dataMem = AppDataAccess()
    val services = AppServices(dataBase)
    val api = AppApi(services)

    val app = api.routes

    val jettyServer = app.asServer(Jetty(port))

    jettyServer.start()

    logger.info("Server started at port $port")
}
