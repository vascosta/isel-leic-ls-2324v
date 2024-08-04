import {getContent, getPathParam} from "./utils/utils.js";
import {
    gameSessionsLimit,
    renderCreateGameForm,
    renderGameDetails, renderGameSessions,
    renderGamesSearch,
    renderSearchedGames
} from "../views/GameViews.js";
import {fetchGameById, fetchSearchGames} from "../http/GameHttp.js";
import {fetchSessions} from "../http/SessionHttp.js";
import {getSearchParameters} from "../views/utils/utils.js";

async function createGame(){
    const mainContent = getContent("mainContent")
    renderCreateGameForm(mainContent)
}

function getGameSearch() {
    const mainContent = getContent("mainContent")
    const playerHomeContent = getContent("playerHomeContent")
    renderGamesSearch(
        mainContent,
        () => getSearchedGames(mainContent, playerHomeContent)
    )
}

async function getSearchedGames(mainContent, playerHomeContent) {
    const params = getSearchParameters()

    const genres = params.genres.map(genre => genre.toLowerCase()).join(",")
    const developer = params.developer.replaceAll(" ", "-")
    const name = params.name.replaceAll(" ", "-")

    const limit = document.getElementById("limit").value

    const gameList = await fetchSearchGames(genres, developer, name,0, parseInt(limit) + 1)
    const hasNext = gameList.games.length > limit
    if (hasNext) gameList.games.pop()

    const data = {
        genres: genres,
        developer: developer,
        name: name,
        limit: limit,
        games: gameList,
        hasNext: hasNext
    }

    await renderSearchedGames(
        playerHomeContent,
        mainContent,
        data,
        () => getGameSearch())
}

async function getGameById() {
    const mainContent = getContent("mainContent")
    const playerHomeContent = getContent("playerHomeContent")

    const id = getPathParam()

    const game = await fetchGameById(id)

    renderGameDetails(mainContent, playerHomeContent, game)
}

async function getGameSessions() {
    const playerHomeContent = getContent("playerHomeContent")
    const mainContent = getContent("mainContent")

    const initialPage = 0

    const gameId = getPathParam(2)
    const game = await fetchGameById(gameId)
    if(!game.message){
        const gameSessions = await fetchSessions(gameId, "", "", "", initialPage, gameSessionsLimit + 1)
        const hasMoreSessions = gameSessions.length > gameSessionsLimit
        if (hasMoreSessions) gameSessions.pop()
        await renderGameSessions(game.name, gameSessions, hasMoreSessions, gameId, initialPage, playerHomeContent, mainContent)
    }else{
        window.location.hash = "notfound"
    }
}

export const gameHandlers = {
    createGame,
    getGameSearch,
    getGameById,
    getGameSessions
}

export default gameHandlers
