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
    csrfToken = res.headers.get("x-csrf-token");
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

export function configuration() {
    return fetchJson("/config");
}
