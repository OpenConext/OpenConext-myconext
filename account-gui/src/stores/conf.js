import {writable} from 'svelte/store';

export const conf = writable({
    migrationUrl: "",
    magicLinkUrl: "",
    domain: "",
    eduIDWebAuthnRedirectSpUrl: "",
    featureWebAuthn: false,
    featureAllowList: false,
    featureWarningEducationalEmailDomain: false
});