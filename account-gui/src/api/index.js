//Internal API
let csrfToken = null;

function validateResponse(res) {

    if (!res.ok) {
        if (res.type === "opaqueredirect") {
            setTimeout(() => window.location.reload(), 100);
            return res;
        }
        throw res;
    }
    csrfToken = res.headers.get("X-CSRF-TOKEN");
    return res.json();
}

function validFetch(path, options) {
    const fetchOptions = {
        ...options,
        credentials: "same-origin",
        redirect: "manual",
        headers: {
            Accept: "application/json",
            "Content-Type": "application/json",
            "X-CSRF-TOKEN": csrfToken
        }
    };
    return fetch(path, fetchOptions).then(res => validateResponse(res));
}

function fetchJson(path, options = {}) {
    return validFetch(path, options);
}

function postPutJson(path, body, method) {
    return fetchJson(path, {method, body: JSON.stringify(body)});
}

//Base
export function magicLinkNewUser(email, givenName, familyName, rememberMe, authenticationRequestId) {
    const body = {user: {email, givenName, familyName}, authenticationRequestId, rememberMe};
    return postPutJson("/myconext/api/idp/magic_link_request", body, "POST");
}

export function magicLinkExistingUser(email, password, rememberMe, usePassword, authenticationRequestId) {
    const body = {user: {email, password}, authenticationRequestId, rememberMe, usePassword};
    return postPutJson("/myconext/api/idp/magic_link_request", body, "PUT");
}
export function configuration() {
    return fetchJson("/config");
}
