import {
    renderCreateSession,
    renderEditSession,
    renderSession,
    renderSessions,
    setSessionsContent
} from "../../views/SessionViews.js";
import router from "../../router/router.js";
import sessionHandlers from "../../handlers/SessionHandlers.js";

describe('Session Tests', () => {

    router.addRouteHandler("sessions/{id}", sessionHandlers.getSession)
    router.addRouteHandler("session/create", sessionHandlers.createSession)
    router.addRouteHandler("session/edit/{id}", sessionHandlers.editSession)
    router.addRouteHandler("sessions/search", sessionHandlers.getSessions)

    const host = {id: 1, name: "Joaquim"}
    const lookingPlayer = {id: 2, name: "Manuel"}
    const participatingPlayer = {id: 3, name: "Maria"}
    const session = {
        players: [host, participatingPlayer],
        capacity: 4,
        date: "2024-12-04",
        game: {id: 1, game: "Minecraft"},
        hostId: 1
    }
    const fullSession = {
        players: [host, participatingPlayer],
        capacity: 2,
        date: "2024-12-04",
        game: {id: 1, game: "Minecraft"},
        hostId: 1
    }
    const sessionId = 1

    describe('Session Handler Tests', function () {
        it('should find getSession handle', function () {

            const handler = router.getRouteHandler("sessions/{id}")

            handler.name.should.be.equal("getSession")
        });

        it('should find createSession handler', function () {

            const handler = router.getRouteHandler("session/create")

            handler.name.should.be.equal("createSession")
        });

        it('should find createSession handler', function () {

            const handler = router.getRouteHandler("session/edit/{id}")

            handler.name.should.be.equal("editSession")
        });

        it('should find searchSession handler', function () {

            const handler = router.getRouteHandler("sessions/search")

            handler.name.should.be.equal("getSession")
        });
    })

    describe('Session View Tests',() => {
        it('should show session info if not logged in', () => {

            const playerHomeContent = document.createElement("div");
            playerHomeContent.setAttribute("id", "playerHomeContent")

            const mainContent = document.createElement("div");
            mainContent.setAttribute("id", "mainContent");

            renderSession(session, sessionId, mainContent, playerHomeContent, false)

            playerHomeContent.innerHTML.should.be.equal(
                '<a href="#sessions/search">Search Sessions</a>'
            )

            mainContent.innerHTML.should.be.equal(
                '<div>' +
                '<div><strong>Date: </strong>2024-12-04</div>' +
                '<div><strong>Capacity: </strong>4</div>' +
                '<div><strong>Game: </strong><a href="#games/1">Minecraft</a></div>' +
                '<div><strong>Players: </strong><a href="#players/1">Joaquim</a>, <a href="#players/3">Maria</a></div>' +
                '<div><strong>Host: </strong><a href="#players/1">Joaquim</a></div>' +
                '</div>'
            )
        });

        it('should show session query', () => {
            const mainContent = document.createElement("div");
            mainContent.setAttribute("id", "mainContent");

            renderSessions(mainContent)

            mainContent.innerHTML.should.be.equal(
                '<div id="searchSession">' +
                '<form id="searchSessionForm">' +
                '<label id="gameLabel">Game</label>' +
                '<input list="gameList" type="input" id="game">' +
                '<datalist id="gameList"></datalist>' +
                '<label id="date">Date of session</label>' +
                '<input name="date" type="date" id="date">' +
                '<label id="state">State of session</label>' +
                '<select name="state" id="state">' +
                '<option value=""></option>' +
                '<option value="open">open</option>' +
                '<option value="close">close</option>' +
                '</select>' +
                '<label id="playerLabel">Player</label>' +
                '<input list="playerList" type="input" id="player">' +
                '<datalist id="playerList"></datalist>' +
                '<label id="paging">Max p/page</label>' +
                '<select name="paging" id="paging">' +
                '<option value="10">10</option>' +
                '<option value="20">20</option>' +
                '<option value="30">30</option>' +
                '</select>' +
                '<button><span class="material-symbols-outlined">Search</span></button>' +
                '</form>' +
                '<table>' +
                '<tr>' +
                '<td>Date</td><td>Capacity</td><td>Game</td><td>Players</td><td>Host</td><td>Details</td>' +
                '</tr>' +
                '</table>' +
                '<div id="paging">' +
                '<button disabled=""><span class="material-symbols-outlined">arrow_back</span></button>' +
                '<button disabled=""><span class="material-symbols-outlined">arrow_forward</span></button>' +
                '</div>' +
                '</div>'
            )
        });
        it('should show join button if player is not in session', () => {
            const playerHomeContent = document.createElement("div");
            playerHomeContent.setAttribute("id", "playerHomeContent")

            const mainContent = document.createElement("div");
            mainContent.setAttribute("id", "mainContent");

            renderSession(session, sessionId, mainContent, playerHomeContent, true, lookingPlayer)

            playerHomeContent.innerHTML.should.be.equal(
                '<a href="#sessions/search">Search Sessions</a>'
            )

            mainContent.innerHTML.should.be.equal(
                '<div>' +
                '<div><strong>Date: </strong>2024-12-04</div>' +
                '<div><strong>Capacity: </strong>4</div>' +
                '<div><strong>Game: </strong><a href="#games/1">Minecraft</a></div>' +
                '<div><strong>Players: </strong><a href="#players/1">Joaquim</a>, <a href="#players/3">Maria</a></div>' +
                '<div><strong>Host: </strong><a href="#players/1">Joaquim</a></div>' +
                '<button>Join</button>' +
                '</div>'
            )
        })
        it('should show edit and delete button if player is the host', () => {

            const playerHomeContent = document.createElement("div");
            playerHomeContent.setAttribute("id", "playerHomeContent")

            const mainContent = document.createElement("div");
            mainContent.setAttribute("id", "mainContent");

            renderSession(session, sessionId, mainContent, playerHomeContent, true, host)

            playerHomeContent.innerHTML.should.be.equal(
                '<a href="#sessions/search">Search Sessions</a>'
            )

            mainContent.innerHTML.should.be.equal(
                '<div>' +
                '<div><strong>Date: </strong>2024-12-04</div>' +
                '<div><strong>Capacity: </strong>4</div>' +
                '<div><strong>Game: </strong><a href="#games/1">Minecraft</a></div>' +
                '<div><strong>Players: </strong><a href="#players/1">Joaquim</a>, <a href="#players/3">Maria</a></div>' +
                '<div><strong>Host: </strong><a href="#players/1">Joaquim</a></div>' +
                '<button>Delete</button>' +
                '<button>Edit</button>' +
                '</div>'
            )
        })
        it('should show leave button if player is in the session', () => {

            const playerHomeContent = document.createElement("div");
            playerHomeContent.setAttribute("id", "playerHomeContent")

            const mainContent = document.createElement("div");
            mainContent.setAttribute("id", "mainContent");

            renderSession(session, sessionId, mainContent, playerHomeContent, true, participatingPlayer)

            playerHomeContent.innerHTML.should.be.equal(
                '<a href="#sessions/search">Search Sessions</a>'
            )

            mainContent.innerHTML.should.be.equal(
                '<div>' +
                '<div><strong>Date: </strong>2024-12-04</div>' +
                '<div><strong>Capacity: </strong>4</div>' +
                '<div><strong>Game: </strong><a href="#games/1">Minecraft</a></div>' +
                '<div><strong>Players: </strong><a href="#players/1">Joaquim</a>, <a href="#players/3">Maria</a></div>' +
                '<div><strong>Host: </strong><a href="#players/1">Joaquim</a></div>' +
                '<button>Leave</button>' +
                '</div>'
            )
        })
        it('should show session create', () => {
            const playerHomeContent = document.createElement("div");
            playerHomeContent.setAttribute("id", "playerHomeContent")

            const mainContent = document.createElement("div");
            mainContent.setAttribute("id", "mainContent");

            renderCreateSession(mainContent)

            mainContent.innerHTML.should.be.equal(
                '<form id="createSessionForm">' +
                '<label id="gameLabel">Game</label>' +
                '<input list="gameList" type="input" id="game">' +
                '<datalist id="gameList"></datalist>' +
                '<label id="date">Date of session</label>' +
                '<input name="date" type="datetime-local" id="date">' +
                '<label id="capacity">Capacity</label>' +
                '<input name="capacity" type="number" id="capacity">' +
                '<button>Create</button>' +
                '</form>'
            )
        })
        it('should show session edit/update parameters', () => {
            const playerHomeContent = document.createElement("div");
            playerHomeContent.setAttribute("id", "playerHomeContent")

            const mainContent = document.createElement("div");
            mainContent.setAttribute("id", "mainContent");

            renderEditSession(session, sessionId, mainContent)

            mainContent.innerHTML.should.be.equal(
                '<form id="editSessionForm">' +
                '<label id="date">Date of session</label>' +
                '<input name="date" type="datetime-local" id="date">' +
                '<label id="capacity">Capacity</label>' +
                '<input name="capacity" type="number" id="capacity">' +
                '<label id="gameLabel">Game</label>' +
                '<input list="gameList" type="input" id="game">' +
                '<datalist id="gameList"></datalist>' +
                '<button><span class="material-symbols-outlined">edit</span></button>' +
                '</form>'
            )
        })
        it('should not show join button on full session', () => {
            const playerHomeContent = document.createElement("div");
            playerHomeContent.setAttribute("id", "playerHomeContent")

            const mainContent = document.createElement("div");
            mainContent.setAttribute("id", "mainContent");

            renderSession(fullSession, sessionId, mainContent, playerHomeContent, true, lookingPlayer)

            mainContent.innerHTML.should.be.equal(
                '<div>' +
                '<div><strong>Date: </strong>2024-12-04</div>' +
                '<div><strong>Capacity: </strong>2</div>' +
                '<div><strong>Game: </strong><a href="#games/1">Minecraft</a></div>' +
                '<div><strong>Players: </strong><a href="#players/1">Joaquim</a>, <a href="#players/3">Maria</a></div>' +
                '<div><strong>Host: </strong><a href="#players/1">Joaquim</a></div>' +
                '</div>'
            )
        })
    });
});
