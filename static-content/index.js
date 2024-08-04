import router from "./router/router.js";
import playerHandlers from "./handlers/PlayerHandlers.js";
import gameHandlers from "./handlers/GameHandlers.js";
import sessionHandlers from "./handlers/SessionHandlers.js";
import homeHandlers from "./handlers/HomeHandlers.js";
import {renderHomeTitle} from "./views/HomeViews.js";
import {getContent} from "./handlers/utils/utils.js";
import {authHandler} from "./auth/auth.js";

window.addEventListener('load', loadHandler)
window.addEventListener('hashchange', hashChangeHandler)

function loadHandler(){

    renderHomeTitle(getContent("homeContent"))

    router.addRouteHandler("home", homeHandlers.getHome)
    router.addRouteHandler("signup", homeHandlers.getSignup)
    router.addRouteHandler("login", homeHandlers.getLogin)
    router.addRouteHandler("notfound", homeHandlers.getNotFound)
    router.addRouteHandler("players/{id}", playerHandlers.getPlayerInfo)
    router.addRouteHandler("players/{id}/sessions", playerHandlers.getPlayerSessions)
    router.addRouteHandler("game/create", authHandler(gameHandlers.createGame))
    router.addRouteHandler("games/search", gameHandlers.getGameSearch)
    router.addRouteHandler("games/search/{name}", gameHandlers.getGameSearch)
    router.addRouteHandler("games/{id}", gameHandlers.getGameById)
    router.addRouteHandler("games/{id}/sessions", gameHandlers.getGameSessions)
    router.addRouteHandler("sessions/search", sessionHandlers.getSessions)
    router.addRouteHandler("sessions/{id}", sessionHandlers.getSession)
    router.addRouteHandler("session/create", authHandler(sessionHandlers.createSession))
    router.addRouteHandler("session/edit/{id}", authHandler(sessionHandlers.editSession))
    router.addDefaultNotFoundRouteHandler(() => window.location.hash = "notfound")

    hashChangeHandler()
}

function hashChangeHandler(){

    const path =  window.location.hash.replace("#", "")

    const handler = router.getRouteHandler(path)

    handler()
}
