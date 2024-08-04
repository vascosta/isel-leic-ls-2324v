import router from "../../router/router.js";
import { gameHandlers } from "../../handlers/GameHandlers.js";
import {
    renderCreateGameForm,
    renderGameDetails, renderGameSessions,
    renderGamesSearch,
    renderSearchedGames
} from "../../views/GameViews.js";

describe('Game Tests', function () {

    router.addRouteHandler("game/create", gameHandlers.createGame)
    router.addRouteHandler("games/search", gameHandlers.getGameSearch)
    router.addRouteHandler("games/{id}", gameHandlers.getGameById)
    router.addRouteHandler("games/{id}/sessions", gameHandlers.getGameSessions)

    describe('Create Game Handler Tests', function () {
        it('should find createGame handle', function () {

            const handler = router.getRouteHandler("game/create")

            handler.name.should.be.equal("createGame")
        });

        it('should not find any handler', function () {

            const handler = router.getRouteHandler("games/create/game")

            handler.name.should.be.equal("notFoundRouteHandler")
        });
    })

    describe('Search Games Tests', function () {
        it('should find getGameSearch handle', function () {

            const handler = router.getRouteHandler("games/search")

            handler.name.should.be.equal("getGameSearch")
        });

        it('should not find any handler', function () {

            const handler = router.getRouteHandler("game/search")

            handler.name.should.be.equal("notFoundRouteHandler")
        });
    })

    describe('Get Game By ID Tests', function () {
        it('should find getGameById handle', function () {

            const handler = router.getRouteHandler("games/{id}")

            handler.name.should.be.equal("getGameById")
        });

        it('should not find any handler', function () {

            const handler = router.getRouteHandler("game/3")

            handler.name.should.be.equal("notFoundRouteHandler")
        });

        it('should find getGameSessions handle', function () {

            const handler = router.getRouteHandler("games/{id}/sessions")

            handler.name.should.be.equal("getGameSessions")
        })
    })

    describe('Game View Tests', async function () {
        it('should show create game form', function () {
            const mainContent = document.createElement("div")
            mainContent.setAttribute("id", "mainContent")

            renderCreateGameForm(mainContent)

            mainContent.innerHTML.should.be.equal(
                '<h1>Create a New Game!</h1>' +
                '<div id="createGame">' +
                '<div id="gameCreateDetails">' +
                '<div id="gameSpecs">' +
                '<div id="gameName"><h3>Name</h3><input type="text" id="name"></div>' +
                '<div id="gameDeveloper"><h3>Developer</h3><input type="text" id="developer"></div>' +
                '</div>' +
                '<div id="gameGenres">' +
                '<h3>Genres</h3>' +
                '<div id="gameGenresList">' +
                '<div><input type="checkbox" id="RPG" value="RPG"><label for="RPG">RPG</label></div>' +
                '<div><input type="checkbox" id="Adventure" value="Adventure"><label for="Adventure">Adventure</label></div>' +
                '<div><input type="checkbox" id="Shooter" value="Shooter"><label for="Shooter">Shooter</label></div>' +
                '<div><input type="checkbox" id="TurnBased" value="TurnBased"><label for="TurnBased">TurnBased</label></div>' +
                '<div><input type="checkbox" id="Action" value="Action"><label for="Action">Action</label></div>' +
                '<div><input type="checkbox" id="Multiplayer" value="Multiplayer"><label for="Multiplayer">Multiplayer</label></div>' +
                '<div><input type="checkbox" id="Puzzle" value="Puzzle"><label for="Puzzle">Puzzle</label></div>' +
                '<div><input type="checkbox" id="Survival" value="Survival"><label for="Survival">Survival</label></div>' +
                '<div><input type="checkbox" id="Sports" value="Sports"><label for="Sports">Sports</label></div>' +
                '<div><input type="checkbox" id="Simulation" value="Simulation"><label for="Simulation">Simulation</label></div>' +
                '<div><input type="checkbox" id="Racing" value="Racing"><label for="Racing">Racing</label></div>' +
                '<div><input type="checkbox" id="BattleRoyale" value="BattleRoyale"><label for="BattleRoyale">BattleRoyale</label></div>' +
                '<div><input type="checkbox" id="Platform" value="Platform"><label for="Platform">Platform</label></div>' +
                '<div><input type="checkbox" id="Sandbox" value="Sandbox"><label for="Sandbox">Sandbox</label></div>' +
                '<div><input type="checkbox" id="Horror" value="Horror"><label for="Horror">Horror</label></div>' +
                '</div>' +
                '</div>' +
                '</div><input type="button" id="newGame" value="Create Game"></div>'
            )
        })

        it('should show game search form', function () {

            const mainContent = document.createElement("div")
            mainContent.setAttribute("id", "mainContent")

            renderGamesSearch(mainContent, gameHandlers.getGameSearch)

            mainContent.innerHTML.should.be.equal(
                '<h1>Search for your favourite games!</h1>' +
                '<form id="gamesSearchForm">' +
                '<div id="gameGenres">' +
                '<h3>Genres</h3>' +
                '<div id="gameGenresList">' +
                '<div><input type="checkbox" id="RPG" value="RPG"><label for="RPG">RPG</label></div>' +
                '<div><input type="checkbox" id="Adventure" value="Adventure"><label for="Adventure">Adventure</label></div>' +
                '<div><input type="checkbox" id="Shooter" value="Shooter"><label for="Shooter">Shooter</label></div>' +
                '<div><input type="checkbox" id="TurnBased" value="TurnBased"><label for="TurnBased">TurnBased</label></div>' +
                '<div><input type="checkbox" id="Action" value="Action"><label for="Action">Action</label></div>' +
                '<div><input type="checkbox" id="Multiplayer" value="Multiplayer"><label for="Multiplayer">Multiplayer</label></div>' +
                '<div><input type="checkbox" id="Puzzle" value="Puzzle"><label for="Puzzle">Puzzle</label></div>' +
                '<div><input type="checkbox" id="Survival" value="Survival"><label for="Survival">Survival</label></div>' +
                '<div><input type="checkbox" id="Sports" value="Sports"><label for="Sports">Sports</label></div>' +
                '<div><input type="checkbox" id="Simulation" value="Simulation"><label for="Simulation">Simulation</label></div>' +
                '<div><input type="checkbox" id="Racing" value="Racing"><label for="Racing">Racing</label></div>' +
                '<div><input type="checkbox" id="BattleRoyale" value="BattleRoyale"><label for="BattleRoyale">BattleRoyale</label></div>' +
                '<div><input type="checkbox" id="Platform" value="Platform"><label for="Platform">Platform</label></div>' +
                '<div><input type="checkbox" id="Sandbox" value="Sandbox"><label for="Sandbox">Sandbox</label></div>' +
                '<div><input type="checkbox" id="Horror" value="Horror"><label for="Horror">Horror</label></div>' +
                '</div>' +
                '</div>' +
                '<div id="gamesNameDeveloper">' +
                '<h3>Name</h3><input type="text" id="name">' +
                '<h3>Developer</h3><input type="text" id="developer">' +
                '<select name="limit" id="limit">' +
                '<option value="2">2</option>' +
                '<option value="10">10</option>' +
                '<option value="15">15</option>' +
                '<option value="20">20</option>' +
                '</select>' +
                '<input type="button" id="" value="Search">' +
                '</div>' +
                '</form>'
            )
        });

        it('should show game search results without next page', async function () {

            const genres = ["RPG","Adventure"]
            const developer = "Nintendo"
            const limit = 5
            const gameList = {
                games: [{
                    id: 4,
                    name: "The Legend Of Zelda",
                    developer: "Nintendo",
                    genres: ["RPG","Adventure"]
                }]
            }

            const data = {
                genres: genres,
                developer: developer,
                limit: limit,
                games: gameList,
                hasNext: false
            }

            const playerHomeContent = document.createElement("div")
            playerHomeContent.setAttribute("id", "playerHomeContent")
            const mainContent = document.createElement("div")
            mainContent.setAttribute("id", "mainContent")

            await renderSearchedGames(
                playerHomeContent,
                mainContent,
                data,
                () => gameHandlers.getGameSearch
            )

            playerHomeContent.innerHTML.should.be.equal(
                `<a href="#games/search" class="link-secondary">Search Games</a>`
            )

            mainContent.innerHTML.should.be.equal(
                '<div id="gamesSearchResult">' +
                '<div id="games">' +
                '<ul>' +
                '<li>' +
                '<a href="#games/4">The Legend Of Zelda</a>' +
                '</li>' +
                '</ul>' +
                '</div>' +
                '</div>'
            )
        });

        it('should show game search results with next page', async function () {

            const genres = ["RPG","Adventure"]
            const developer = "Nintendo"
            const limit = 5
            const gameList = {
                games: [{
                    id: 4,
                    name: "The Legend Of Zelda",
                    developer: "Nintendo",
                    genres: ["RPG","Adventure"]
                }]
            }

            const data = {
                genres: genres,
                developer: developer,
                limit: limit,
                games: gameList,
                hasNext: true
            }

            const playerHomeContent = document.createElement("div")
            playerHomeContent.setAttribute("id", "playerHomeContent")
            const mainContent = document.createElement("div")
            mainContent.setAttribute("id", "mainContent")

            await renderSearchedGames(
                playerHomeContent,
                mainContent,
                data,
                () => gameHandlers.getGameSearch
            )

            playerHomeContent.innerHTML.should.be.equal(
                `<a href="#games/search" class="link-secondary">Search Games</a>`
            )

            mainContent.innerHTML.should.be.equal(
                '<div id="gamesSearchResult">' +
                '<div id="games">' +
                '<ul>' +
                '<li><a href="#games/4">The Legend Of Zelda</a></li>' +
                '</ul>' +
                '</div>' +
                '<div id="paging"><button disabled=""><span class="material-symbols-outlined">arrow_back</span></button>' +
                '<button><span class="material-symbols-outlined">arrow_forward</span></button>' +
                '</div>' +
                '</div>'
            )
        });

        it('should show game details', function () {

            const game = { id: 1, name: "The Legend of Zelda", developer: "Nintendo", genres: ["RPG","ADVENTURE"] }

            const playerHomeContent = document.createElement("div")
            playerHomeContent.setAttribute("id", "playerHomeContent")
            const mainContent = document.createElement("div")
            mainContent.setAttribute("id", "mainContent")

            renderGameDetails(mainContent, playerHomeContent, game)

            playerHomeContent.innerHTML.should.be.equal(
                '<a href="#games/search" class="link-secondary">Search Games</a>'
            )

            mainContent.innerHTML.should.be.equal(
                '<div id="gameDetails">' +
                '<h1>The Legend of Zelda</h1>' +
                '<div>' +
                '<div><strong>Developer: </strong>Nintendo</div>' +
                '<div><strong>Genres: </strong>RPG, ADVENTURE</div>' +
                '</div>' +
                '</div><a href="#games/1/sessions">Game Sessions</a>'
            )
        });

        it('should show game sessions', function () {

            const gameName = "Kirby's Bizarre Adventure"
            const gameId = 4
            const gameSessions = [{
                id: 10,
                capacity: 3,
                date: "2024-12-04T00:00:00Z",
                gameId: 4,
                players: [1],
                host: 1
            }]
            const hasMoreSessions = false
            const currentPage = 0

            const playerHomeContent = document.createElement("div")
            playerHomeContent.setAttribute("id", "playerHomeContent")
            const mainContent = document.createElement("div")
            mainContent.setAttribute("id", "mainContent")

            renderGameSessions(gameName, gameSessions, hasMoreSessions, gameId, currentPage, playerHomeContent, mainContent)

            playerHomeContent.innerHTML.should.be.equal(
                '<a href="#sessions/search">Search Sessions</a>'
            )

            mainContent.innerHTML.should.be.equal(
                '<div id="gameSessions">' +
                '<h1>Kirby\'s Bizarre Adventure\'s sessions</h1>' +
                '<ul>' +
                '<li><a href="#sessions/10">Session 10</a></li>' +
                '</ul>' +
                '</div>' +
                '<div id="paging">' +
                '<button disabled=""><span class="material-symbols-outlined">arrow_back</span></button>' +
                '<button disabled=""><span class="material-symbols-outlined">arrow_forward</span></button>' +
                '</div>'
            )
        });
    })
})



