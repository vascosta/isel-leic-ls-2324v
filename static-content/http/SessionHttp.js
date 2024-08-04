import {getSessionStorageToken} from "../auth/auth.js";

const API_URL = "https://img-ls-2324-2-42d-g10.onrender.com/api/"
const API_SESSION_URL = API_URL + "session"

async function fetchWithToken(method, endpoint, body = null) {
    const headers = {
        "Authorization": "Bearer " + getSessionStorageToken(),
        "Content-Type": "application/json"
    };

    const requestOptions = {
        method: method,
        headers: headers,
        credentials: 'include'
    };

    if (body) requestOptions.body = JSON.stringify(body);

    return fetch(endpoint, requestOptions).then(async res => {
        const contentType = res.headers.get('content-type');
        if (contentType && contentType.includes('application/json')) {
            return await res.json();
        } else {
            return res;
        }
    });
}

export async function fetchEditSession(sessionId, update){
    return await fetchWithToken("PATCH", `${API_SESSION_URL}/${sessionId}`, update)
}

export async function fetchDeleteSession(sessionId) {
    return await fetchWithToken("DELETE", `${API_SESSION_URL}/${sessionId}`);
}

export async function fetchLeaveSession(sessionId) {
    return await fetchWithToken("PATCH", `${API_SESSION_URL}/${sessionId}/player`);
}

export async function fetchJoinSession(sessionId) {
    return await fetchWithToken("PATCH", `${API_SESSION_URL}/${sessionId}/players`);
}

export async function fetchCreateSession(create){
    return await fetchWithToken("POST", `${API_SESSION_URL}`, create)
}

export async function fetchSessions(gid, date, state, pid, skip, limit){
    const params = new URLSearchParams();
    if (gid) params.append('gid', gid);
    if (date) params.append('date', date);
    if (state) params.append('state', state);
    if (pid) params.append('pid', pid);
    if (skip) params.append('skip', skip);
    if (limit) params.append('limit', limit);

    return await fetchWithToken('GET', `${API_SESSION_URL}/search?`+params)
}

export async function fetchSession(sessionId){
    return await fetchWithToken('GET', `${API_SESSION_URL}/info/${sessionId}`)
}