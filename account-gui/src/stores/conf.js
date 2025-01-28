import {writable} from 'svelte/store';

export const conf = writable({
    basePath: "",
    magicLinkUrl: "",
    continueAfterLoginUrl: "",
    domain: "",
    eduIDWebAuthnRedirectSpUrl: "",
    featureWebAuthn: false,
    featureAllowList: false,
    idpBaseUrl: "",
    featureWarningEducationalEmailDomain: false,
    featureIdVerify: false,
    emailSpamThresholdSeconds: 5,
    mobileAppRedirect: ""
});

export const links = writable({
    userLink: true,
    displayBackArrow: true
});