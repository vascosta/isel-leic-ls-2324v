import {h1, li, ul} from "../dsl/dsl.js";
import {clearContent} from "../handlers/utils/utils.js";
import {renderSessions, renderSessionSearchTitle} from "./SessionViews.js";
import {fetchSessions} from "../http/SessionHttp.js";
import {mixText, newButton, newDiv, newLink, newList, newListItem, newText} from "./utils/utils.js";

export const playerSessionsLimit = 10;

export function renderPlayerInfo(playerInfo, playerId, playerHomeContent, mainContent) {
    const h1PlayerInfo = h1("Player Info")
    const ulPlayerInfo = newDiv('playerInfo',
        mixText("Name: ", playerInfo.name),
        mixText("Email: ", playerInfo.email)
    )
    const h3PlayerSessions = newLink("#players/" + playerId + "/sessions", "My Sessions")

    mainContent.replaceChildren(h1PlayerInfo, ulPlayerInfo, h3PlayerSessions)
    clearContent("playerHomeContent")
}

export async function renderPlayerSessions(
    playerName,
    playerSessions,
    hasMoreSessions,
    playerId,
    currentPage,
    playerHomeContent,
    mainContent
) {
    const playerSesssionsDiv = newDiv('playerSessions')
    const h1PlayerSessions = h1(playerName + "'s Sessions")
    console.log(playerSessions)
    const ulPlayerSessions = newList(
        ...playerSessions.map(session => newListItem(newLink("#sessions/" + session.id, "Session " + session.id)))
    )

    const aSessionSearchTittle = renderSessionSearchTitle()
    aSessionSearchTittle.addEventListener("click", () => renderSessions(mainContent))

    const paging = newDiv('paging')

    const nextButton = newButton(
        "arrow_forward",
        !hasMoreSessions,
        async () => {
            currentPage++;
            const newPlayerSessions = await fetchSessions("", "", "", playerId, currentPage * playerSessionsLimit, playerSessionsLimit + 1)
            const hasMoreSessions = newPlayerSessions.length > playerSessionsLimit
            if (hasMoreSessions) newPlayerSessions.pop()
            await renderPlayerSessions(playerName, newPlayerSessions, hasMoreSessions, playerId, currentPage, playerHomeContent, mainContent);
        }
    );

    const prevButton = newButton(
        "arrow_back",
        currentPage === 0,
        async () => {
            if (currentPage > 0) {
                currentPage--;
            }
            const newPlayerSessions = await fetchSessions("", "", "", playerId, currentPage * playerSessionsLimit, playerSessionsLimit)
            await renderPlayerSessions(playerName, newPlayerSessions, true, playerId, currentPage, playerHomeContent, mainContent);
        }
    );

    if (playerSessions.length > 0) {
        paging.append(prevButton, nextButton)
    } else {
        const noSessionsMessage = newText("You haven't joined any sessions yet...")
        paging.append(noSessionsMessage)
    }
    playerSesssionsDiv.append(h1PlayerSessions, ulPlayerSessions, paging)
    mainContent.replaceChildren(playerSesssionsDiv)
    playerHomeContent.replaceChildren(aSessionSearchTittle)
}
