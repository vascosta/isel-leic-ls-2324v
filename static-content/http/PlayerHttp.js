import {deleteToken, getSessionStorageToken, updateSessionStorage} from "../auth/auth.js";

const API_URL = "https://img-ls-2324-2-42d-g10.onrender.com/api/"
const API_PLAYER_URL = API_URL + "player/"
const API_PLAYERS_URL = API_URL + "players/"
const API_PLAYER_HOME_URL = API_URL + "home/"

export function createPlayer(player) {
    return fetch(API_PLAYER_URL, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(player)
    }).then(async res => {
            const body = await res.json()
            if (!body.message)updateSessionStorage(body.token)
            return body
        }
    )
}

export function loginPlayer(player) {
    return fetch(API_URL + "login", {
        method: "PATCH",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(player)
    }).then(async res => {
            const body = await res.json()
            if (!body.message) updateSessionStorage(body.token)
            return body
    })
}

export function logoutPlayer() {
    return fetch(API_URL + "logout", {
        method: "PATCH",
        headers: {
            "Content-Type": "application/json",
            "Authorization": "Bearer " + getSessionStorageToken()
        },
        credentials: "include"
    }).then(async res => {
        const contentType = res.headers.get('content-type');
        if (contentType && contentType.includes('application/json')) {
            return await res.json()
        }
        else deleteToken()
    })
}

export function fetchPlayerHome(token) {
    return fetch(API_PLAYER_HOME_URL + token, {
        method: "GET",
        headers: {
            "Content-Type": "application/json"
        }
    }).then(res => res.json())
}

export function fetchPlayerInfo(id) {
    return fetch(API_PLAYER_URL + id, {
        method: "GET",
        headers: {
            "Content-Type": "application/json",
            "Authorization": "Bearer " + getSessionStorageToken()
        },
        credentials: "include"
    }).then(res => res.json())
}

export function fetchPlayersByName(name, skip, limit) {
    return fetch(API_PLAYERS_URL+'search?'+
        'name=' + name +
        '&skip=' + skip +
        '&limit=' + limit
        , {
        method: "GET",
        headers: {
            "Content-Type": "application/json",
            "Authorization": "Bearer " + getSessionStorageToken()
        },
    }).then(res => res.json())
}