import {writable} from 'svelte/store';

export const conf = writable({
    magicLinkUrl: "",
    continueAfterLoginUrl: "",
    domain: "",
    eduIDWebAuthnRedirectSpUrl: "",
    featureWebAuthn: false,
    featureAllowList: false,
    featureWarningEducationalEmailDomain: false,
    featureIdVerify: false,
    emailSpamThresholdSeconds: 5,
    mobileAppRedirect: ""
});

export const links = writable({
    userLink: true,
    displayBackArrow: true
});