const routes = []
let notFoundRouteHandler = () => { throw "Route handler for unknown routes not defined" }

function addRouteHandler(path, handler){
    const uriArgs = path.split('/')
    routes.push( { path, handler, uriArgs } )
}
function addDefaultNotFoundRouteHandler(notFoundRH) {
    notFoundRouteHandler = notFoundRH
}

function getRouteHandler(path){
    const uriArgs = path.split('/')
    const route = routes.find(r => {
        if (r.path === path) return true
        if (r.uriArgs.length !== uriArgs.length) return false
        return r.uriArgs.every((part, index) => {
            if (part.startsWith('{') && part.endsWith('}')) return true
            return part === uriArgs[index]
        })
    })
    return route ? route.handler : notFoundRouteHandler
}


const router = {
    addRouteHandler,
    getRouteHandler,
    addDefaultNotFoundRouteHandler
}

export default router