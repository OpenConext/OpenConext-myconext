//Internal API
import I18n from "i18n-js";

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

export function configuration() {
    return fetchJson("/config");
}

export function updateUser(user) {
    return postPutJson("/myconext/api/sp/update", user, "PUT");
}

export function updateSecurity(userId, currentPassword, newPassword) {
    const body = {userId, currentPassword, newPassword};
    return postPutJson("/myconext/api/sp/security", body, "PUT");
}

export function startWebAuthFlow() {
    return fetchJson("/myconext/api/sp/security/webauthn");
}

export function deleteUser() {
    const fetchOptions = {
        credentials: "same-origin",
        redirect: "manual"
    };
    return fetchDelete("/myconext/api/sp/delete").then(() => fetch("/Shibboleth.sso/Logout", fetchOptions));
}

export function deleteLinkedAccount(linkedAccount) {
    return postPutJson("/myconext/api/sp/institution", linkedAccount, "PUT");
}

export function deleteService(service) {
    return postPutJson("/myconext/api/sp/service", service, "PUT");
}


export function startLinkAccountFlow() {
    return fetchJson("/myconext/api/sp/oidc/link");
}

export function logout() {
    const fetchOptions = {
        credentials: "same-origin",
        redirect: "manual"
    };
    return fetchJson("/myconext/api/sp/logout").then(() => fetch("/Shibboleth.sso/Logout", fetchOptions));
}

export function forgetMe() {
    return fetchDelete("/myconext/api/sp/forget");
}

export function mergeAfterMigration() {
    return fetchJson("/myconext/api/sp/migrate/merge");
}

export function proceedAfterMigration() {
    return fetchJson("/myconext/api/sp/migrate/proceed");
}