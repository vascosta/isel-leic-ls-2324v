export function a(text, href, isPrimary = false) {
    const a = document.createElement("a")
    const textNode = document.createTextNode(text)
    a.appendChild(textNode)
    a.href = href
    a.className = isPrimary ? "link-primary" : "link-secondary"
    return a
}

export function h1(text) {
    const h1 = document.createElement("h1")
    const textNode = document.createTextNode(text)
    h1.appendChild(textNode)
    return h1
}

export function h3(text) {
    const h3 = document.createElement("h3")
    const textNode = document.createTextNode(text)
    h3.appendChild(textNode)
    return h3
}

export function ul(...li) {
    const ul = document.createElement("ul")
    li.forEach(liItem => ul.appendChild(liItem))
    return ul
}

export function li(text, href = null) {
    const li = document.createElement("li")
    const textNode = document.createTextNode(text)
    li.appendChild(textNode)
    if (onclick !== undefined && href) {
        li.addEventListener("click",
            function () {
                window.location.href = href
            }
        )
    }
    return li
}

export function liOfElem(element) {
    const li = document.createElement("li")
    li.appendChild(element)
    return li
}

export function strong(text) {
    const strong = document.createElement("strong")
    const textNode = document.createTextNode(text)
    strong.appendChild(textNode)
    return strong
}

export function input(type, id, value = null) {
    const input = document.createElement("input")
    input.setAttribute("type", type)
    input.setAttribute("id", id)

    if (value !== null) {
        input.setAttribute("value", value)
    }

    return input
}

export function label(ref, name) {
    const label = document.createElement("label")
    label.setAttribute("for", ref)

    const text = document.createTextNode(name)
    label.appendChild(text)

    return label
}

export function button(symbol, disabled, onclick) {
    const button = document.createElement('button');
    button.textContent = symbol;
    button.disabled = disabled;
    button.onclick = onclick;
    return button;
}