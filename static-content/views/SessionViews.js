import {
    getFormParams, isDatePast,
    newButton,
    newDatalist,
    newInput,
    newInputList,
    newLabel,
    newLink, newTextWithLink,
    newSelectBox,
    newTable, newTableColumn, newText, selectListHydrate, submitForm, newTextWithLinks, newTableRow, newDiv, mixText,
} from "./utils/utils.js";
import {
    fetchCreateSession,
    fetchDeleteSession,
    fetchEditSession,
    fetchJoinSession,
    fetchLeaveSession,
    fetchSessions
} from "../http/SessionHttp.js";
import {clearContent} from "../handlers/utils/utils.js";
import {fetchSearchGamesByName} from "../http/GameHttp.js";
import {fetchPlayersByName} from "../http/PlayerHttp.js";

export function renderSessionSearchTitle() {
    return newLink("#sessions/search", "Search Sessions")
}

export async function setSessionsContent(sessions, sessionsHolder) {
    let count = 0;
    while (sessionsHolder.children.length > 1) {
        sessionsHolder.removeChild(sessionsHolder.lastChild);
    }
    sessions.forEach(session => {
        const hostPlayer = session.players.find(player => player.id === session.hostId);
        newTableRow(
            sessionsHolder,
            newTableColumn(session.date),
            newTableColumn(session.capacity),
            newTableColumn(newLink('#games/' + session.game.id, session.game.game)),
            newTableColumn(session.players.map(player => `${player.name}`).join(', ')),
            newTableColumn(newLink('#players/' + session.hostId, hostPlayer ? hostPlayer.name : 'Unknown')),
            newTableColumn(newLink('#sessions/' + session.id, '...'))
        )
        count++;
    });
    return count;
}

async function getParamsAndFetchSessions(gameOptions, playerOptions, page, form) {
    const gameName = document.getElementById('game').value;
    const game = gameOptions.find(game => game.name === gameName);
    const playerName = document.getElementById('player').value;
    const player = playerOptions.find(player => player.name === playerName);
    let gameId = '';
    let playerId = '';
    if (game) gameId = game.id;
    if (player) playerId = player.id;
    let params = getFormParams(form, ['date', 'state']);
    const paging = parseInt(form.elements['paging'].value);
    const skip = page * paging;
    const limit = paging + 1
    return await fetchSessions(gameId, params[0], params[1], playerId, skip, limit);
}

export function renderSessions(mainContent){
    const tableElement = newTable(['Date', 'Capacity', 'Game', 'Players', 'Host', 'Details'])
    let page = 0
    let isNextAvailable = false
    let gameOptions = []
    let playerOptions = []
    let isSearched = false

    const updateButtonStates = () => {
        previousPage.disabled = page === 0 || !isSearched
        nextPage.disabled = !isNextAvailable || !isSearched
    }

    const form = document.createElement('form');
    form.id = 'searchSessionForm';

    newLabel('gameLabel', 'Game', form);
    let gameInputList = newInputList('game', 'gameList', null, null, form);
    newDatalist('gameList', gameOptions, form);

    newLabel('date', 'Date of session', form);
    newInput('date', null, null, 'date', null, form);

    newLabel('state', 'State of session', form);
    newSelectBox('state', ['', 'open', 'close'], form);

    newLabel('playerLabel', 'Player', form);
    let playerInputList = newInputList('player', 'playerList', null, null, form);
    newDatalist('playerList', playerOptions, form);

    newLabel('paging', 'Max p/page', form);
    newSelectBox('paging', [10, 20, 30], form);

    newButton('Search', false, ('click', async function (event) {
        event.preventDefault();
        page = 0
        let sessions = await getParamsAndFetchSessions(gameOptions, playerOptions, page, form);
        isNextAvailable = sessions.length > parseInt(form.elements['paging'].value);
        if(isNextAvailable) sessions.pop()
        await setSessionsContent(sessions, tableElement);
        isSearched = true
        updateButtonStates()
    }), form);

    const previousPage = newButton('arrow_back', true, async function (event) {
        event.preventDefault();
        page -= 1;
        let sessions = await getParamsAndFetchSessions(gameOptions, playerOptions, page, form)
        sessions.pop()
        await setSessionsContent(sessions, tableElement);
        isNextAvailable = true;
        updateButtonStates()
    });

    const nextPage = newButton('arrow_forward', true, async function (event) {
        event.preventDefault();
        page += 1
        const paging = parseInt(form.elements['paging'].value);
        let sessions = await getParamsAndFetchSessions(gameOptions, playerOptions, page, form)
        if (sessions.length <= paging) isNextAvailable = false;
        if (isNextAvailable) sessions.pop();
        await setSessionsContent(sessions, tableElement);
        updateButtonStates()
    });

    const paging = newDiv('paging', previousPage, nextPage)
    const div = newDiv('searchSession', form, tableElement, paging)

    mainContent.replaceChildren(div);
    selectListHydrate(gameInputList, 'gameList', gameOptions, fetchSearchGamesByName, 'games')
    selectListHydrate(playerInputList, 'playerList', playerOptions, fetchPlayersByName, 'players')
    clearContent("playerHomeContent");
    updateButtonStates()
}

export function renderSession(session, id, mainContent, playerHomeContent, isLoggedIn, player){
    const hostPlayer = session.players.find(p => p.id === session.hostId);
    const playersLi = session.players.map(p => newLink("#players/" + p.id, p.name));
    const sessionInfo = document.createElement('div')
    sessionInfo.append(
        mixText("Date: ", session.date),
        mixText("Capacity: ", session.capacity),
        newTextWithLink("Game: ", session.game.game, "#games/" + session.game.id, true),
        newTextWithLinks("Players: ", playersLi, true),
        newTextWithLink("Host: ", (hostPlayer ? hostPlayer.name : "Unknown"), "#players/" + hostPlayer.id, true)
    )

    if (isLoggedIn) {
        const isHost = session.hostId === player.id;
        const isPastDate = isDatePast(session.date);
        if (isHost) {
            newButton("Delete", false, ('click', async function(event) {
                event.preventDefault();
                const res = await fetchDeleteSession(id);
                if (res.message) alert(res.message);
                else location.href = '#players/' + player.id + '/sessions';
            }), sessionInfo, false);
            if (!isPastDate) {
                newButton("Edit", false, ('click', async function(event) {
                    event.preventDefault();
                    location.href = '#session/edit/' + id;
                }), sessionInfo, false);
            }
        } else if (!isPastDate) {
            const isPlayerInSession = session.players.some(p => p.id === player.id);
            const action = isPlayerInSession ? "Leave" : "Join";
            const fetchFunction = isPlayerInSession ? fetchLeaveSession : fetchJoinSession;
            if(action === "Leave" || (action === "Join" && session.capacity > session.players.length)){
                newButton(action, false, ('click', async function(event) {
                    event.preventDefault();
                    const res = await fetchFunction(id);
                    if (res.message) alert(res.message);
                    else window.location.reload();
                }), sessionInfo, false);
            }
        }
    }

    const aSessionsSearchTitle = renderSessionSearchTitle();

    playerHomeContent.replaceChildren(aSessionsSearchTitle);
    mainContent.replaceChildren(sessionInfo);
}

export function renderCreateSession(mainContent){
    const form = document.createElement('form');
    form.id = 'createSessionForm';

    let gameOptions = []

    newLabel('gameLabel', 'Game', form);
    let gameInputList = newInputList('game', 'gameList', null, null, form);
    newDatalist('gameList', gameOptions, form);

    newLabel('date', 'Date of session', form);
    newInput('date', null, null, 'datetime-local', null, form);

    newLabel('capacity', 'Capacity', form);
    newInput('capacity', null, null, 'number', null, form);

    newButton('Create', false,async function (event) {
        const data = submitForm(event, gameOptions, form)
        const res = await fetchCreateSession(data);
        if (res.message) alert(res.message)
        else location.href = '#sessions/'+res.sessionId
    }, form, false);

    mainContent.replaceChildren(form);
    selectListHydrate(gameInputList, 'gameList', gameOptions, fetchSearchGamesByName, 'games')
    clearContent("playerHomeContent");
}

export function renderEditSession(session, sessionId, mainContent){
    const form = document.createElement('form')
    form.id = 'editSessionForm'

    const sessionDate = session.date.slice(0, 16);
    let gameOptions = []

    newLabel('date', 'Date of session', form);
    newInput('date', null, null, 'datetime-local', sessionDate, form);

    newLabel('capacity', 'Capacity', form);
    newInput('capacity', null, null, 'number', session.capacity, form);

    newLabel('gameLabel', 'Game', form);
    let gameInputList = newInputList('game', 'gameList', null, null, form);
    newDatalist('gameList', gameOptions, form);

    newButton('edit', false,async function (event) {
        const data = submitForm(event, gameOptions, form)
        const res = await fetchEditSession(sessionId, data);
        if (res.message) alert(res.message)
        else location.href = '#sessions/'+sessionId
    }, form, true);

    mainContent.replaceChildren(form);
    selectListHydrate(gameInputList, 'gameList', gameOptions, fetchSearchGamesByName, 'games')
    clearContent("playerHomeContent")
}