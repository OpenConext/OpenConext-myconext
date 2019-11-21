//Internal API
function validateResponse(res) {

    if (!res.ok) {
        if (res.type === "opaqueredirect") {
            setTimeout(() => window.location.reload(), 100);
            return res;
        }
        throw res;
    }

    return res.json();
}

function validFetch(path, options) {
    const fetchOptions = {
        ...options,
        credentials: "same-origin",
        redirect: "manual",
        headers: {
            Accept: "application/json",
            "Content-Type": "application/json"
        }
    };
    return fetch(path, fetchOptions).then(res => validateResponse(res));
}
function fetchDelete(path) {
    return validFetch(path, {method: "delete"});
}

function fetchJson(path, options = {}) {
    return validFetch(path, options);
}

function postPutJson(path, body, method) {
    return fetchJson(path, {method, body: JSON.stringify(body)});
}

//Base
export function me() {
    return fetchJson("/myconext/api/sp/me");
}

export function config() {
    return fetchJson("/config");
}

export function updateUser(user) {
    return postPutJson("/myconext/api/sp/update", user, "PUT");
}

export function updateSecurity(userId, updatePassword, clearPassword, currentPassword, newPassword) {
    const body = {userId, updatePassword, clearPassword, currentPassword, newPassword};
    return postPutJson("/myconext/api/sp/security", body, "PUT");
}

export function deleteUser(user) {
    return fetchDelete("/myconext/api/sp/delete");
}
