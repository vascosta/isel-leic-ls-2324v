import {clearContent, getContent} from "../handlers/utils/utils.js";
import {a, button, h1, h3, input, label, li, ul} from "../dsl/dsl.js";
import {fetchSessions} from "../http/SessionHttp.js";
import {renderSessions, renderSessionSearchTitle} from "./SessionViews.js";
import {
    getSearchParameters,
    mixText,
    newButton,
    newDiv,
    newLink,
    newList,
    newListItem,
    newSelectBox,
    newText
} from "./utils/utils.js";
import {createGame, fetchGameById, fetchSearchGames} from "../http/GameHttp.js";

export const GENRES = [
    "RPG",  "Adventure", "Shooter", "TurnBased",
    "Action", "Multiplayer", "Puzzle", "Survival",
    "Sports", "Simulation", "Racing", "BattleRoyale",
    "Platform", "Sandbox", "Horror"
]

export const gameSessionsLimit = 10

export function renderGamesSearchTitle() {
    return a("Search Games", "#games/search", false)
}

export function renderCreateGameForm(mainContent) {
    const createGame = newDiv('createGame')
    const h1Create = h1("Create a New Game!")

    const gameCreateDetails = newDiv('gameCreateDetails',
        newDiv('gameSpecs',
            newDiv('gameName', h3("Name"), input("text", "name")),
            newDiv('gameDeveloper', h3("Developer"), input("text", "developer"))
        ),
        renderGenresCheckBox()
    )
    const searchButton = renderCreateGameButton()

    createGame.append(gameCreateDetails, searchButton)
    mainContent.replaceChildren(h1Create, createGame)
    clearContent("playerHomeContent")
}

function renderCreateGameButton() {
    const button = input("button", "newGame", "Create Game")
    button.addEventListener("click", async function () {
        const name = document.getElementById("name").value
        const params = getSearchParameters()

        const game = {
            name: name,
            developer: params.developer,
            genres: params.genres.map(genre => genre.toUpperCase())
        }

        const newGame = await createGame(game)

        if (!newGame.message) {
            const mainContent = getContent("mainContent")
            const playerHomeContent = getContent("playerHomeContent")

            const id = parseInt(newGame.id)
            const gameById = await fetchGameById(id)

            renderGameDetails(mainContent, playerHomeContent, gameById)
        } else {
            alert("Invalid Game!")
        }
    })

    return button
}

export function renderGamesSearch(mainContent, getSearchedGames) {
    const h1Search = h1("Search for your favourite games!")

    const searchForm = renderGamesSearchForm()
    const updatedGDForm = renderGamesSearchSubmitButton(searchForm.form, searchForm.div, getSearchedGames)

    mainContent.replaceChildren(h1Search, updatedGDForm)
    clearContent("playerHomeContent")
}

function renderGamesSearchForm() {
    const form = document.createElement("form")
    form.id = "gamesSearchForm"

    const divGenres = renderGenresCheckBox()
    const divDeveloper = renderTextBoxesAndLimit()

    form.append(divGenres, divDeveloper)

    return {
        form: form,
        div: divDeveloper
    }
}

function renderGenresCheckBox() {
    return newDiv('gameGenres',
        h3("Genres"),
        newDiv('gameGenresList', ...GENRES.map(genre =>
            newDiv(null,
                input("checkbox", genre, genre),
                label(genre, genre)
            )
        )))
}

function renderTextBoxesAndLimit() {
    const divTextBoxes = newDiv("gamesNameDeveloper")

    const h3Name = h3("Name")
    const nameTextBox = input("text", "name")
    divTextBoxes.appendChild(h3Name)
    divTextBoxes.appendChild(nameTextBox)

    const h3Developer = h3("Developer")
    const developerTextBox = input("text", "developer")
    divTextBoxes.appendChild(h3Developer)
    divTextBoxes.appendChild(developerTextBox)

    const limit = newSelectBox('limit', [2, 10, 15, 20]);

    divTextBoxes.appendChild(limit)

    return divTextBoxes
}

function renderGamesSearchSubmitButton(form, div, onClick) {
    const button = input("button", "", "Search")
    button.addEventListener("click", function () {
        onClick()
    })

    div.appendChild(button)
    form.appendChild(div)

    return form
}

let page = 0
let isNextAvailable = true

export async function renderSearchedGames(playerHomeContent, mainContent, data, onClick) {
    const div = newDiv('gamesSearchResult')

    const aGamesSearchTittle = renderGamesSearchTitle()
    aGamesSearchTittle.addEventListener("click", () => onClick())

    const divGames = newDiv('games')
    const initialGames = renderListGameSearch(data.games)

    divGames.appendChild(initialGames)
    div.appendChild(divGames)

    if (data.hasNext) {
        const divButtons = newDiv('paging')

        const previousPage = newButton('arrow_back', true, ('click', async function (event) {
            event.preventDefault();
            page -= 1;
            const skip = page * parseInt(data.limit);
            const result = await fetchSearchGames(data.genres, data.developer, data.name, skip, data.limit)
            const elem = renderListGameSearch(result)
            isNextAvailable = true;
            divGames.replaceChildren(elem)
            updateButtonStates()
        }), divButtons)

        const nextPage = newButton('arrow_forward', true, ('click', async function (event) {
            event.preventDefault();
            if (isNextAvailable) {
                page += 1;
                const skip = page * parseInt(data.limit);
                const result = await fetchSearchGames(
                    data.genres,
                    data.developer,
                    data.name,
                    skip,
                    parseInt(data.limit) + 1
                )
                const hasNext = result.games.length > data.limit
                if (hasNext) result.games.pop()
                const elem = renderListGameSearch(result)
                isNextAvailable = hasNext;
                divGames.replaceChildren(elem)
                updateButtonStates()
            }
        }), divButtons)

        const updateButtonStates = () => {
            previousPage.disabled = page === 0
            nextPage.disabled = !isNextAvailable
        }

        div.appendChild(divButtons)
        updateButtonStates()
    }

    playerHomeContent.replaceChildren(aGamesSearchTittle)
    mainContent.replaceChildren(div)
}

function renderListGameSearch(result) {
    if (result.games.length !== 0) {
        return newList(
            ...result.games.map(g => {
                return newListItem(newLink("#games/" + g.id, g.name))
            })
        )
    } else {
        return h1("No Games Found")
    }
}

export function renderGameDetails(mainContent, playerHomeContent, game) {
    const aGamesSearchTittle = renderGamesSearchTitle()

    const gameDetails = newDiv('gameDetails')

    const h1Game = h1(game.name)
    const joinToGenres = game.genres.join(", ")

    const container = newDiv(null,
        mixText("Developer: ", game.developer),
        mixText("Genres: ", joinToGenres),
    )

    const aGameSessions = newLink("#games/" + game.id + "/sessions", "Game Sessions")

    gameDetails.append(h1Game, container)
    playerHomeContent.replaceChildren(aGamesSearchTittle)
    mainContent.replaceChildren(gameDetails, aGameSessions)
}

export async function renderGameSessions(
    gameName,
    gameSessions,
    hasMoreSessions,
    gameId,
    currentPage,
    playerHomeContent,
    mainContent
) {
    const gameSessionsDiv = newDiv('gameSessions')
    const h1GameSessions = h1(gameName + "'s sessions")
    const ulGameSessions = newList(
        ...gameSessions.map(session =>
            newListItem(
                newLink("#sessions/" + session.id, "Session " + session.id)
            )
        )
    )

    const aSessionsSearchTitle = renderSessionSearchTitle()
    aSessionsSearchTitle.addEventListener("click", () => { renderSessions(mainContent)} )

    let paging = newDiv('paging')

    const nextButton = newButton(
        "arrow_forward",
        !hasMoreSessions,
        async () => {
            currentPage++;
            const newGameSessions = await fetchSessions(gameId, "", "", "", currentPage * gameSessionsLimit, gameSessionsLimit + 1)
            const hasMoreSessions = newGameSessions.length > gameSessionsLimit
            if (hasMoreSessions) newGameSessions.pop()
            await renderGameSessions(newGameSessions, hasMoreSessions, gameId, currentPage, playerHomeContent, mainContent);
        }
    );

    const prevButton = newButton(
        "arrow_back",
        currentPage === 0,
        async () => {
            if (currentPage > 0) {
                currentPage--;
            }
            const newGameSessions = await fetchSessions(gameId, "", "", "", currentPage * gameSessionsLimit, gameSessionsLimit)
            await renderGameSessions(newGameSessions, true, gameId, currentPage, playerHomeContent, mainContent);
        }
    );

    if(gameSessions.length > 0) {
        paging.append(prevButton, nextButton)
    }else{
        const noSessionsMessage = newText("There aren't any sessions for this game yet...")
        paging.append(noSessionsMessage)
    }

    gameSessionsDiv.append(h1GameSessions, ulGameSessions)
    mainContent.replaceChildren(gameSessionsDiv, paging)
    playerHomeContent.replaceChildren(aSessionsSearchTitle)
}