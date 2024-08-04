import {fetchSession} from "../http/SessionHttp.js";
import {renderCreateSession, renderEditSession, renderSession, renderSessions} from "../views/SessionViews.js";
import {getContent, getPathParam} from "./utils/utils.js";
import {getPlayerHome} from "../auth/auth.js";

async function getSessions(){
    const mainContent = getContent("mainContent")
    renderSessions(mainContent)
}

async function getSession(){
    const mainContent = getContent("mainContent")
    const playerHomeContent = getContent("playerHomeContent")

    let sessionId = getPathParam();
    let session = await fetchSession(sessionId)

    const player = await getPlayerHome()
    if (player) {
        renderSession(session, sessionId, mainContent, playerHomeContent, true, player)
        return
    }

    renderSession(session, sessionId, mainContent, playerHomeContent, false)
}

async function createSession() {
    const mainContent = getContent("mainContent")
    renderCreateSession(mainContent)
}

async function editSession(){
    const mainContent = getContent("mainContent")

    let sessionId = getPathParam();
    let session = await fetchSession(sessionId)

    const player = await getPlayerHome()
    if (player && session.hostId === player.id) {
        renderEditSession(session, sessionId, mainContent, player)
        return
    }

    alert("You are not the session's host")

    location.href = '#home'
}

export const sessionHandlers = {
    getSessions,
    getSession,
    createSession,
    editSession
}

export default sessionHandlers