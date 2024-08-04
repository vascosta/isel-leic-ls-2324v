import {fetchPlayerHome} from "../http/PlayerHttp.js";

export function authHandler(handler) {
    return function(){
        if (getSessionStorageToken()) {
            handler()
        }
        else {
            window.location.hash = "signup"
        }
    }
}

export function getSessionStorageToken() {
    return window.sessionStorage.getItem("token")
}

export function updateSessionStorage(token) {
    window.sessionStorage.setItem("token", token)
}

export function deleteToken() {
    window.sessionStorage.removeItem("token")
}

export async function getPlayerHome(){
    const playerToken = getSessionStorageToken()
    if (playerToken) {
        const playerHome = await fetchPlayerHome(playerToken)
        if (playerHome && !playerHome.message) {
            return playerHome
        }
    }
    return null
}