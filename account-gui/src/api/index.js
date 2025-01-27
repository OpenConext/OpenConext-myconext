//Internal API
import I18n from "../locale/I18n";
import {status} from "../constants/loginStatus";

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
    options.headers = {
        Accept: "application/json",
        "Content-Type": "application/json",
        "Accept-Language": I18n.currentLocale(),
        "X-CSRF-TOKEN": csrfToken
    };
    return fetch(path, options).then(res => validateResponse(res));
}

function fetchJson(path, options = {}) {
    return validFetch(path, options);
}

function postPutJson(path, body, method) {
    return fetchJson(path, {method, body: JSON.stringify(body)});
}

//Base
export function magicLinkNewUser(email, givenName, familyName, authenticationRequestId) {
    const body = {user: {email, givenName, familyName}, authenticationRequestId};
    return postPutJson("/myconext/api/idp/magic_link_request", body, "POST");
}

export function magicLinkExistingUser(email, authenticationRequestId) {
    const body = {user: {email}, authenticationRequestId};
    return postPutJson("/myconext/api/idp/magic_link_request", body, "PUT");
}

export function passwordExistingUser(email, password, authenticationRequestId) {
    const body = {user: {email, password}, authenticationRequestId, usePassword: true};
    return postPutJson("/myconext/api/idp/magic_link_request", body, "PUT");
}

export function resendMagicLinkMail(id) {
    return fetchJson(`/myconext/api/idp/resend_magic_link_request?id=${id}`);
}

export function institutionalEmailDomains() {
    return fetchJson("/myconext/api/idp/email/domain/institutional")
}

export function userInfo(hash) {
    return fetchJson(`/myconext/api/idp/me/${hash}`);
}

export function allowedEmailDomains() {
    return fetchJson("/myconext/api/idp/email/domain/allowed")
}

export function configuration() {
    return fetchJson("/config");
}

export function webAuthnRegistration(token) {
    return postPutJson("/myconext/api/idp/security/webauthn/registration", {token}, "POST");
}

export function knownAccount(email) {
    return postPutJson("/myconext/api/idp/service/email", {email}, "POST");
}


export function webAuthnRegistrationResponse(token, name, credentials, request) {
    return postPutJson("/myconext/api/idp/security/webauthn/registration", {token, name, credentials, request}, "PUT");
}

export function webAuthnStartAuthentication(email, authenticationRequestId, test) {
    return postPutJson("/myconext/api/idp/security/webauthn/authentication", {
        email,
        authenticationRequestId,
        test
    }, "POST");
}

export function webAuthnTryAuthentication(credentials, authenticationRequestId, rememberMe) {
    const body = {credentials, authenticationRequestId, rememberMe};
    return postPutJson("/myconext/api/idp/security/webauthn/authentication", body, "PUT");
}

export function successfullyLoggedIn(id) {
    if (typeof document.hidden !== "undefined" && document.hidden) {
        return Promise.resolve(status.NOT_LOGGED_IN)
    }
    return fetchJson(`/myconext/api/idp/security/success?id=${id}`);
}

//We can safely cache this for the duration of the session
export function fetchServiceName(id) {
    const serviceName = sessionStorage.getItem("serviceName");
    if (serviceName) {
        return Promise.resolve({name: serviceName})
    } else {
        return fetchJson(`/myconext/api/idp/service/name/${id}`)
            .then(json => {
                if (json.name) {
                    sessionStorage.setItem("serviceName", json.name);
                }
                return Promise.resolve(json);
            })
            .catch(() => Promise.resolve({name: "?"}));
    }

}

export function fetchServiceNameByHash(hash) {
    return fetchJson(`/myconext/api/idp/service/hash/${hash}`).catch(() => Promise.resolve({name: "?"}))
}

//Tiqr
export function startEnrollment(hash) {
    return fetchJson(`/tiqr/start-enrollment?hash=${hash}`);
}

export function pollEnrollment(enrollmentKey) {
    return fetchJson(`/tiqr/poll-enrollment?enrollmentKey=${enrollmentKey}`)
}

export function fetchQrCode(url) {
    return postPutJson("/tiqr/qrcode", {url}, "POST");
}

export function generateBackupCode(hash) {
    return fetchJson(`/tiqr/generate-backup-code?hash=${hash}`)
}

export function textPhoneNumber(hash, phoneNumber) {
    return postPutJson(`/tiqr/send-phone-code?hash=${hash}`,{phoneNumber}, "POST")
}

export function manualResponse(sessionKey, response) {
    return postPutJson("/tiqr/manual-response",{sessionKey, response}, "POST")
}

export function validatePhoneCode(hash, phoneVerification) {
    return postPutJson(`/tiqr/verify-phone-code?hash=${hash}`,{phoneVerification}, "POST")
}

export function startTiqrAuthentication(email, authenticationRequestId) {
    return postPutJson("/tiqr/start-authentication", {
        email,
        authenticationRequestId
    }, "POST");
}

export function pollAuthentication(sessionKey, id) {
    return fetchJson(`/tiqr/poll-authentication?sessionKey=${sessionKey}&id=${id}`)
}

export function rememberMe(hash) {
    return postPutJson("/tiqr/remember-me", {hash}, "PUT");
}

export function iDINIssuers() {
    return fetchJson("/myconext/api/sp/idin/issuers");
}

export function reportError(error) {
    return postPutJson("/myconext/api/sp/error", error, "post");
}

