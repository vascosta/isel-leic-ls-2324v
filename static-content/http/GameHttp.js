import {getSessionStorageToken} from "../auth/auth.js";

const API_BASE_URL = "https://img-ls-2324-2-42d-g10.onrender.com/api/"

export function createGame(game) {
    return fetch(API_BASE_URL + "game", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": "Bearer " + getSessionStorageToken()
        },
        body: JSON.stringify(game)
    }).then(res => res.json())
}

export function fetchSearchGames(genres, developer, name, skip, limit) {
    return fetch(
        API_BASE_URL + "games" +
        "?genres=" + genres +
        "&developer=" + developer +
        "&name=" + name +
        "&skip=" + skip +
        "&limit=" + limit
    ).then(rsp => rsp.json())
}

export function fetchGameById(id) {
    return fetch(API_BASE_URL + "game/" + id)
        .then(rsp => rsp.json())
}

export function fetchSearchGamesByName(name, skip, limit){
    return fetch(
        API_BASE_URL + "games" +
        "?name=" + name +
        "&skip=" + skip +
        "&limit=" + limit
    ).then(rsp => rsp.json())
}