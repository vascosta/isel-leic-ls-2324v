import router from "../../router/router.js";
import { playerHandlers } from "../../handlers/PlayerHandlers.js";
import {renderPlayerInfo, renderPlayerSessions} from "../../views/PlayerViews.js";

describe('Player Tests', function () {

    router.addRouteHandler("players/{id}", playerHandlers.getPlayerInfo)
    router.addRouteHandler("players/{id}/sessions", playerHandlers.getPlayerSessions)

    describe('Player View Tests', function () {

        it('should show player info', function () {

            const playerInfo = {name: "Joaquim", email: "joaquim@email.com"}
            const playerId = 1904

            const playerHomeContent = document.createElement("div")
            playerHomeContent.setAttribute("id", "playerHomeContent")
            const mainContent = document.createElement("div")
            mainContent.setAttribute("id", "mainContent")

            renderPlayerInfo(playerInfo, playerId, playerHomeContent, mainContent)

            playerHomeContent.innerHTML.should.be.equal("")

            mainContent.innerHTML.should.be.equal(
                '<h1>Player Info</h1>' +
                '<div id="playerInfo">' +
                '<div><strong>Name: </strong>Joaquim</div>' +
                '<div><strong>Email: </strong>joaquim@email.com</div>' +
                '</div>' +
                '<a href="#players/1904/sessions">My Sessions</a>'
            )
        });
        it('should show player sessions', function () {

            const playerName = "José"
            const playerId = 1904
            const playerSessions = [{id: 1}, {id: 2}]
            const hasMoreSessions = false
            const currentPage = 0

            const playerHomeContent = document.createElement("div")
            playerHomeContent.setAttribute("id", "playerHomeContent")
            const mainContent = document.createElement("div")
            mainContent.setAttribute("id", "mainContent")

            renderPlayerSessions(playerName, playerSessions, hasMoreSessions, playerId, currentPage, playerHomeContent, mainContent)

            playerHomeContent.innerHTML.should.be.equal('<a href="#sessions/search">Search Sessions</a>')

            mainContent.innerHTML.should.be.equal(
                '<div id="playerSessions"' +
                '><h1>José\'s Sessions</h1>' +
                '<ul>' +
                '<li><a href="#sessions/1">Session 1</a></li>' +
                '<li><a href="#sessions/2">Session 2</a></li>' +
                '</ul>' +
                '<div id="paging">' +
                '<button disabled=""><span class="material-symbols-outlined">arrow_back</span></button>' +
                '<button disabled=""><span class="material-symbols-outlined">arrow_forward</span></button>' +
                '</div>' +
                '</div>'
            )
        });
    })

    describe('Player Handlers Tests', function () {
        it('should find getPlayerInfo handle', function () {

            const handler = router.getRouteHandler("players/{id}")

            handler.name.should.be.equal("getPlayerInfo")
        });

        it('should find getPlayerSessions handle', function () {

            const handler = router.getRouteHandler("players/{id}/sessions")

            handler.name.should.be.equal("getPlayerSessions")
        });
    })
})



