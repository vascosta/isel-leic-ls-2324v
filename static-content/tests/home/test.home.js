import router from "../../router/router.js";
import homeHandlers from "../../handlers/HomeHandlers.js";
import {renderHomeTitle, renderLogin, renderPlayerHome, renderSignup} from "../../views/HomeViews.js";

describe('Home Tests', function () {

    router.addRouteHandler("home", homeHandlers.getHome)
    router.addRouteHandler("signup", homeHandlers.getSignup)
    router.addRouteHandler("login", homeHandlers.getLogin)
    router.addRouteHandler("notofund", homeHandlers.getNotFound)

    describe('Home View Tests', function () {
        it('should show home', function () {

            const homeContent = document.createElement("div")
            homeContent.setAttribute("id", "homeContent")

            renderHomeTitle(homeContent)

            homeContent.innerHTML.should.be.equal('<a href="#home" class="link-primary">Home</a>')
        });

        it('should show player home logged in', function () {

            const playerHome = {id: 1, name: "Joaquim"}
            const isLoggedIn = true

            const mainContent = document.createElement('div')
            mainContent.id = "mainContent"

            const playerHomeContent = document.createElement("div")
            playerHomeContent.setAttribute("id", "playerHomeContent")

            renderPlayerHome(mainContent, playerHome, playerHomeContent, isLoggedIn)

            mainContent.innerHTML.should.be.equal('Welcome, Joaquim')

            playerHomeContent.innerHTML.should.be.equal(
                '<a href="#games/search" class="link-secondary">Search Games</a>' +
                '<a href="#sessions/search">Search Sessions</a>' +
                '<a href="#session/create" class="link-secondary">Create session</a>' +
                '<a href="#game/create" class="link-secondary">Create game</a>' +
                '<a href="#players/1" class="link-secondary">Profile</a>' +
                '<button><span class="material-symbols-outlined">Logout</span></button>'
            )
        });

        it('should show player home not logged in', function () {

            const playerHome = null
            const isLoggedIn = false

            const mainContent = document.createElement('div')
            mainContent.id = "mainContent"

            const playerHomeContent = document.createElement("div")
            playerHomeContent.setAttribute("id", "playerHomeContent")

            renderPlayerHome(mainContent, playerHome, playerHomeContent, isLoggedIn)

            mainContent.innerHTML.should.be.equal('')

            playerHomeContent.innerHTML.should.be.equal(
                '<a href="#signup" class="link-secondary">Signup</a>' +
                '<a href="#login" class="link-secondary">Login</a>' +
                '<a href="#games/search" class="link-secondary">Search Games</a>' +
                '<a href="#sessions/search">Search Sessions</a>'
            )
        });

        it('should show signup', function () {

            const mainContent = document.createElement("div")
            mainContent.setAttribute("id", "mainContent")

            renderSignup(mainContent)

            mainContent.innerHTML.should.be.equal(
                '<h1>Signup</h1>' +
                '<form id="signup">' +
                '<label id="username">Username</label>' +
                '<input name="username" type="text" id="username">' +
                '<label id="email">Email</label>' +
                '<input name="email" type="email" id="email">' +
                '<label id="password">Password</label>' +
                '<input name="password" type="password" id="password">' +
                '<button>Signup</button>' +
                '<a href="#login" class="link-secondary">Already have an account?</a>' +
                '</form>'
            )
        });

        it('should show login', function () {

            const mainContent = document.createElement("div")
            mainContent.setAttribute("id", "mainContent")

            renderLogin(mainContent)

            mainContent.innerHTML.should.be.equal(
                '<h1>Login</h1>' +
                '<form id="login">' +
                '<label id="username">Username</label>' +
                '<input name="username" type="text" id="username">' +
                '<label id="password">Password</label>' +
                '<input name="password" type="password" id="password">' +
                '<button>Login</button>' +
                '<a href="#signup" class="link-secondary">Dont have an account?</a>' +
                '</form>'
            )
        });

        it('should show notfound', function () {

            // TODO
        });
    })

    describe('Home Handlers Tests', function () {
        it('should find getHome handle', function () {

            const handler = router.getRouteHandler("home")

            handler.name.should.be.equal("getHome")
        });

        it('should find getSignup handle', function () {

            const handler = router.getRouteHandler("signup")

            handler.name.should.be.equal("getSignup")
        });

        it('should find getLogin handle', function () {

            const handler = router.getRouteHandler("login")

            handler.name.should.be.equal("getLogin")
        });

        it('should find notFoundRoute handle', function () {

            const handler = router.getRouteHandler("notfound")

            handler.name.should.be.equal("notFoundRouteHandler")
        });
    })
})



