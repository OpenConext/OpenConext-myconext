import {writable} from 'svelte/store';

export const conf = writable({
    myconextBasePath: "",
    magicLinkUrl: "",
    continueAfterLoginUrl: "",
    baseDomain: "",
    myconextWebAuthnRedirectUrl: "",
    featureWebAuthn: false,
    featureAllowList: false,
    accountBaseUrl: "",
    featureWarningEducationalEmailDomain: false,
    featureIdVerify: false,
    featureServiceDeskActive: false,
    emailSpamThresholdSeconds: 5,
    mobileAppRedirect: "",
    captchaEnabled: false,
    captchaSiteKey: null
});

export const links = writable({
    userLink: true,
    displayBackArrow: true
});
