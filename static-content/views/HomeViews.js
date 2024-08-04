import {a, h1, h3} from "../dsl/dsl.js";
import { clearContent } from "../handlers/utils/utils.js";
import { renderGamesSearchTitle } from "./GameViews.js";
import { renderSessionSearchTitle } from "./SessionViews.js";
import {createPlayer, loginPlayer, logoutPlayer} from "../http/PlayerHttp.js";
import {newButton, newInput, newLabel} from "./utils/utils.js";

export function renderHomeTitle(homeContent) {
    const aHome = a("Home", "#home", true)

    homeContent.replaceChildren(aHome)
    clearContent("mainContent")
}

function renderCreateSession() {
    return a("Create session", "#session/create", false)
}

function renderCreateGame() {
    return a("Create game", "#game/create", false)
}

function renderPlayerProfileTitle(playerId) {
    return a("Profile", "#players/" + playerId, false)
}

function renderSignupTitle() {
    return a("Signup", "#signup", false)
}

function renderLoginTitle() {
    return a("Login", "#login", false)

}

export function renderPlayerHome(mainContent, playerHome, playerHomeContent, isLoggedIn) {
    const aGamesSearchTitle = renderGamesSearchTitle()
    const aSessionsSearchTitle = renderSessionSearchTitle()

    if (isLoggedIn) {
        const aCreateSession = renderCreateSession()
        const aCreateGame = renderCreateGame()
        const aPlayerProfile = renderPlayerProfileTitle(playerHome.id)
        const logoutButton = newButton("Logout", false, async function (event) {
            const res = await logoutPlayer()
            if (res != null) alert(res.message)
            else window.location.reload();
        })
        playerHomeContent.replaceChildren(aGamesSearchTitle, aSessionsSearchTitle, aCreateSession, aCreateGame, aPlayerProfile, logoutButton)
        mainContent.replaceChildren("Welcome, " + playerHome.name)
    } else {
        const aSignupTitle = renderSignupTitle()
        const aLoginTitle = renderLoginTitle()
        playerHomeContent.replaceChildren(aSignupTitle, aLoginTitle, aGamesSearchTitle, aSessionsSearchTitle)
    }
}

export function renderSignup(mainContent) {
    const h1Signup = h1("Signup")

    const form = document.createElement('form')
    form.id = 'signup'

    newLabel("username", "Username", form)
    const inputPlayerName = newInput("username", null, null, 'text', "Username", form)
    newLabel("email", "Email", form)
    const inputPlayerEmail = newInput("email", null, null, 'email', "Email@email.com", form)
    newLabel("password", "Password", form)
    const inputPlayerPassword = newInput("password", null, null, 'password', "Password", form)

    newButton("Signup", false, async function (event) {
        event.preventDefault()
        const res = await createPlayer({
            name: inputPlayerName.value,
            email: inputPlayerEmail.value,
            password: inputPlayerPassword.value
        })
        if (res.message) alert(res.message)
        else window.location.hash = "home"
    }, form, false)

    form.append(a("Already have an account?", "#login", false)) // Adds a link to the login page

    mainContent.replaceChildren(h1Signup, form)
    clearContent("playerHomeContent")
}

export function renderLogin(mainContent) {
    const h1Login = h1("Login")

    const form = document.createElement('form')
    form.id = 'login'

    newLabel("username", "Username", form)
    const inputPlayerName = newInput("username", null, null, 'text', "Username", form)
    newLabel("password", "Password", form)
    const inputPlayerPassword = newInput("password", null, null, 'password', "Password", form)

    newButton("Login", false, async function (event) {
        event.preventDefault()
        const res = await loginPlayer({
            name: inputPlayerName.value,
            password: inputPlayerPassword.value
        })
        if (res.message) alert(res.message)
        else window.location.hash = "home"
    }, form, false)

    form.append(a("Dont have an account?", "#signup", false)) // Adds a link to the signup page

    mainContent.replaceChildren(h1Login, form)
    clearContent("playerHomeContent")
}

export function renderNotFound(mainContent) {
    const header1 = h1("Page not found")
    const header2 = h3("Perhaps you mistyped something?")

    mainContent.replaceChildren(header1, header2)
    clearContent("playerHomeContent")
}