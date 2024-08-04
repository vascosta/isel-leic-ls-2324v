import {getContent} from "./utils/utils.js";
import {renderHomeTitle, renderLogin, renderNotFound, renderPlayerHome, renderSignup} from "../views/HomeViews.js";
import {getPlayerHome} from "../auth/auth.js";

async function getHome() {
    const homeContent = getContent("homeContent")
    const mainContent = getContent("mainContent")
    const playerHomeContent = getContent("playerHomeContent")

    renderHomeTitle(homeContent)

    const playerHome = await getPlayerHome()

    if (playerHome) {
        renderPlayerHome(mainContent, playerHome, playerHomeContent, true)
        return
    }
    renderPlayerHome(mainContent, null, playerHomeContent, false)
}

function getSignup() {
    const mainContent = getContent("mainContent")

    renderSignup(mainContent)
}

function getLogin() {
    const mainContent = getContent("mainContent")

    renderLogin(mainContent)
}

function getNotFound() {
    const mainContent = getContent("mainContent")

    renderNotFound(mainContent)
}

export const homeHandlers = {
    getHome,
    getSignup,
    getLogin,
    getNotFound
}

export default homeHandlers;