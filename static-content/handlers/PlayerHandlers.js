import {playerSessionsLimit, renderPlayerInfo, renderPlayerSessions} from "../views/PlayerViews.js"
import {fetchPlayerInfo} from "../http/PlayerHttp.js";
import {getContent, getPathParam} from "./utils/utils.js";
import {fetchSessions} from "../http/SessionHttp.js";

async function getPlayerInfo() {
    const playerHomeContent = getContent("playerHomeContent")
    const mainContent = getContent("mainContent")

    const playerId = getPathParam()
    const playerInfo = await fetchPlayerInfo(playerId)

    renderPlayerInfo(playerInfo, playerId, playerHomeContent, mainContent)
}

async function getPlayerSessions() {
    const playerHomeContent = getContent("playerHomeContent")
    const mainContent = getContent("mainContent")

    const initialPage = 0

    const playerId = getPathParam(2)
    const player = await fetchPlayerInfo(playerId)
    if(!player.message){
        const playerSessions = await fetchSessions("", "", "", playerId, initialPage, playerSessionsLimit + 1)
        const hasMoreSessions = playerSessions.length > playerSessionsLimit
        if (hasMoreSessions) playerSessions.pop()
        await renderPlayerSessions(player.name, playerSessions, hasMoreSessions, playerId, initialPage, playerHomeContent, mainContent)
    }else{
        window.location.hash = "notfound"
    }
}

export const playerHandlers = {
    getPlayerInfo,
    getPlayerSessions
}

export default playerHandlers;