import {isEmpty} from "../utils/Utils";
import I18n from "../locale/I18n";
import {useAppStore} from "../stores/AppStore";
import {paginationQueryParams} from "../utils/Pagination";

//Internal API
function validateResponse(showErrorDialog) {
    return res => {
        if (!res.ok) {
            if (res.type === "opaqueredirect") {
                setTimeout(() => window.location.reload(true), 100);
                return res;
            }
            const error = new Error(res.statusText);
            error.response = res;
            if (showErrorDialog && res.status === 401) {
                window.location.reload(true);
                return;
            }
            if (showErrorDialog) {
                setTimeout(() => {
                    throw error;
                }, 250);
            }
            throw error;
        }
        const sessionAlive = res.headers.get("x-session-alive");
        if (sessionAlive !== "true") {
            window.location.reload(true);
        }
        return res;

    };
}

function validFetch(path, options, headers = {}, showErrorDialog = true) {

    const contentHeaders = {
        "Accept": "application/json",
        "Content-Type": "application/json",
        "Accept-Language": I18n.locale,
        "X-CSRF-TOKEN": useAppStore.getState().csrfToken,
        ...headers
    };
    const fetchOptions = Object.assign({}, {headers: contentHeaders}, options, {
        credentials: "same-origin",
        redirect: "manual",
        changeOrigin: false,
    });
    return fetch(path, fetchOptions).then(validateResponse(showErrorDialog))

}

function fetchJson(path, options = {}, headers = {}, showErrorDialog = true) {
    return validFetch(path, options, headers, showErrorDialog)
        .then(res => res.json());
}

function postPutJson(path, body, method, showErrorDialog = true) {
    const jsonBody = JSON.stringify(body);
    return fetchJson(path, {method: method, body: jsonBody}, {}, showErrorDialog);
}

//Base
export function configuration() {
    return fetchJson("/api/v1/users/config", {}, {}, false);
}

//Users
export function me() {
    return fetchJson("/api/v1/users/me", {}, {}, false);
}

export function resendInvitation(invitationId) {
    return postPutJson(`/api/v1/invitations/${invitationId}`, {}, "PUT");
}
