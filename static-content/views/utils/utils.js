import { strong } from "../../dsl/dsl.js";
import { GENRES } from "../GameViews.js";

export function newDiv(id, ...elements){
    const div = document.createElement('div')
    if(id !== undefined && id !== null) div.id = id
    div.append(...elements)

    return div
}

export function newList(...children) {
    const ul = document.createElement('ul')
    children.forEach(child => ul.appendChild(child))
    return ul
}

export function newListItem(...children) {
    const li = document.createElement('li')
    children.forEach(child => li.appendChild(child))
    return li
}

export function newLink(linkToNavigate, text, elementToAppend = null) {
    const link = document.createElement('a');
    link.href = linkToNavigate;
    link.textContent = text;

    if(elementToAppend != null){
        elementToAppend.appendChild(link)
    }

    return link
}

export function newText(textInput, elementToAppend) {
    const text = newDiv()
    text.textContent = textInput

    if(elementToAppend != null){
        elementToAppend.appendChild(text)
    }
    return text
}

export function newTextWithLinks(label, links, isLabelBold = false, elementToAppend = null) {
    const container = newDiv()
    if(isLabelBold){
        const bold = strong(label)
        container.append(bold);
    }else{
        container.textContent = label
    }

    links.forEach(link => {
        container.appendChild(link);
        container.appendChild(document.createTextNode(', '));
    });

    if (links.length > 0) {
        container.lastChild.textContent = container.lastChild.textContent.slice(0, -2);
    }

    if(elementToAppend != null){
        elementToAppend.appendChild(container)
    }

    return container;
}

export function newTextWithLink(label, text, href, isLabelBold = false, elementToAppend = null) {
    const link = newLink(href, text);
    const container = newDiv()
    if(isLabelBold){
        const bold = strong(label)
        container.append(bold);
    }else{
        container.textContent = label
    }
    container.appendChild(link);

    if(elementToAppend != null){
        elementToAppend.appendChild(container)
    }
    return container;
}

export function newLabel(label, text, elementToAppend = null) {
    const lb = document.createElement('label');
    lb.id = label;
    lb.textContent = text;
    if(elementToAppend != null){
        elementToAppend.appendChild(lb);
    }
    return lb
}

export function newSelectBox(selectLabel, options, elementToAppend = null) {
    const select = document.createElement('select');
    select.name = selectLabel;
    select.id = selectLabel;

    options.forEach(option => {
        const optionElement = document.createElement('option');
        optionElement.value = option;
        optionElement.text = option;
        select.appendChild(optionElement);
    });

    if(elementToAppend != null){
        elementToAppend.appendChild(select);
    }
    return select
}

export function newDatalist(datalistLabel, options, elementToAppend) {
    const datalist = document.createElement('datalist');
    datalist.id = datalistLabel;

    options.forEach(option => {
        const optionElement = document.createElement('option');
        optionElement.value = option.name;
        if (option.id) {
            optionElement.setAttribute('data-id', option.id);
        }
        datalist.appendChild(optionElement);
    });

    if(elementToAppend != null){
        elementToAppend.appendChild(datalist);
    }
    return datalist
}

export function newInputList(name, listName, min, max, elementToAppend = null) {
    const inp = document.createElement('input')
    inp.setAttribute('list', listName);
    if (min != null) inp.min = min;
    if (max != null) inp.max = max;
    inp.type = 'input'
    inp.id = name

    if(elementToAppend != null){
        elementToAppend.appendChild(inp)
    }
    return inp
}

export function newInput(name, min, max, type, defaultValue = null, elementToAppend = null) {
    const inp = document.createElement('input');
    inp.name = name;
    inp.type = type;
    inp.id = name;
    if (defaultValue != null) inp.value = defaultValue;
    if (min != null) inp.min = min;
    if (max != null) inp.max = max;

    if(elementToAppend != null){
        elementToAppend.appendChild(inp)
    }
    return inp
}

export function newButton(text, isDisabled, onclick, elementToAppend = null, isIcon = true) {
    const button = document.createElement('button');
    button.onclick = onclick;
    button.disabled = isDisabled;

    if(text && isIcon){
        const span = document.createElement('span')
        span.className = 'material-symbols-outlined'
        span.textContent = text;
        button.appendChild(span)
    }else{
        button.textContent = text
    }

    if(elementToAppend != null){
        elementToAppend.appendChild(button)
    }
    return button
}

export function newTable(columns) {
    const tableElement = document.createElement('table');
    const tableRow = document.createElement('tr');

    columns.forEach(name => tableRow.appendChild(newTableColumn(name)))

    tableElement.appendChild(tableRow);

    return tableElement;
}

export function newTableRow(elementToAppend = null, ...columns){
    const tableRow = document.createElement('tr');

    columns.forEach(column => tableRow.append(column))

    if(elementToAppend != null){
        elementToAppend.append(tableRow)
    }
    return tableRow
}

export function newTableColumn(content) {
    const tableColumn = document.createElement('td');

    if (typeof content === 'string' || typeof content === 'number') {
        tableColumn.textContent = content;
    } else {
        tableColumn.appendChild(content);
    }
    return tableColumn;
}

export function mixText(boldText, normalText, elementToAppend = null) {
    const div = newDiv()
    const bold = strong(boldText)
    const normal = document.createTextNode(normalText)

    div.appendChild(bold)
    div.appendChild(normal)

    if(elementToAppend != null){
        elementToAppend.appendChild(div)
    }
    return div
}

export function getFormParams(form, listOfParams) {
    return listOfParams.map(e => {
        return form.elements[e].value;
    })
}

export function getSearchParameters() {
    const genres = GENRES.map(genre => {
        const selected = document.getElementById(genre)
        if (selected.checked) return genre
    }
    ).filter(it => it !== undefined)

    const developer = document.getElementById("developer").value

    const name = document.getElementById("name").value

    return {
        genres: genres,
        developer: developer,
        name: name
    }
}

export function submitForm(event, options, form){
    event.preventDefault();
    const gameName = document.getElementById('game').value;
    const game = options.find(game => game.name === gameName);

    const params = getFormParams(form, ['date', 'capacity']);
    const offsetString = calculateOffsetString();
    return {
        'capacity': params[1] ? parseInt(params[1]) : null,
        'gameId': game ? game.id : null,
        'date': params[0] ? params[0] + offsetString: null
    }
}

export function selectListHydrate(source, listSource, options, fetchMethod, arrayPropertyPath, ...predefinedParams) {
    const inputHandler = async function (e) {
        if (e.target.value.length % 2 === 0 || e.target.value === "") return;

        const data = await fetchMethod(...predefinedParams, e.target.value, 0, 5);

        options.length = 0;
        if (arrayPropertyPath) {
            let array = data;
            arrayPropertyPath.split('.').forEach(property => {
                array = array[property];
            });
            options.push(...array);
        } else {
            options.push(...data);
        }

        const list = document.getElementById(listSource);
        list.innerHTML = '';

        options.forEach(option => {
            const optionElement = document.createElement('option');
            optionElement.value = option.name;
            list.appendChild(optionElement);
        });
    };

    source.addEventListener('keypress', inputHandler);
}

export function calculateOffsetString() {
    let offsetInMinutes = new Date().getTimezoneOffset();
    let offsetInHours = offsetInMinutes / 60;
    let offsetHours = Math.abs(Math.floor(offsetInHours));
    let offsetMinutes = Math.abs(offsetInMinutes % 60);
    return (offsetInHours >= 0 ? '-' : '+') +
        (offsetHours < 10 ? '0' : '') + offsetHours + ':' +
        (offsetMinutes < 10 ? '0' : '') + offsetMinutes;
}

export function isDatePast(dateString) {
    const sessionDate = new Date(dateString);
    const currentDate = new Date();
    return sessionDate < currentDate;
}