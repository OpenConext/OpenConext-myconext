import {writable} from 'svelte/store';

export const conf = writable({
    migrationUrl: "",
    magicLinkUrl: "",
    continueAfterLoginUrl: "",
    domain: "",
    eduIDWebAuthnRedirectSpUrl: "",
    featureWebAuthn: false,
    featureAllowList: false,
    featureWarningEducationalEmailDomain: false,
    useExternalValidation: false,
    emailSpamThresholdSeconds: 5
});

export const links = writable({
    userLink: true,
    displayBackArrow: true
});