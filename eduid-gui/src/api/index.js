import I18n from "i18n-js";

//Internal API
let csrfToken = null;

function validateResponse(res) {

    csrfToken = res.headers.get("x-csrf-token") || csrfToken;

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
    options = options || {};
    options.credentials = "same-origin";
    options.redirect = "manual";
    options. headers = {
        Accept: "application/json",
        "Content-Type": "application/json",
        "Accept-Language": I18n.locale,
        "X-CSRF-TOKEN": csrfToken
    }   ;
    return fetch(path, options).then(res => validateResponse(res));
}

function fetchJson(path, options = {}) {
    return validFetch(path, options);
}

export function configuration() {
    return fetchJson("/config");
}
