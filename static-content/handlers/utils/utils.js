export function getPathParam(segment = 1) {
    let path = window.location.href;
    let pathSegments = path.split("/");
    return pathSegments[pathSegments.length - segment]
}

export function getContent(content) {
    return document.getElementById(content)
}

export function clearContent(content) {
    const element = document.getElementById(content)
    if (element !== null) {
        element.replaceChildren()
    }
}