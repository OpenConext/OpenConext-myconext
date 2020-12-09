import {writable} from 'svelte/store';

export const user = writable({
    id: "",
    email: "",
    givenName: "",
    familyName: "",
    guest: true,
    schacHomeOrganization: "",
    uid: "",
    usePassword: false,
    forgottenPassword: false,
    usePublicKey: false,
    rememberMe: true,
    created: 0,
    publicKeyCredentials: [],
    linkedAccounts: [],
    eduIdPerServiceProvider: {},
    oidcTokens: []
});

export const config = writable({
    loginUrl: "",
    baseDomain: "",
    migrationLandingPageUrl: "",
    myConextUrlGuestIdp: "",
    eduIDWebAuthnUrl: "",
    magicLinkUrl: "",
    domain: "",
    featureWebAuthn: false,
    featureOidcTokenAPI: false,
    featureConnections: false
});

export const redirectPath = writable("");

export const duplicatedEmail = writable("");

const createFlash = () => {
    const {subscribe, set} = writable("");

    return {
        subscribe,
        setValue: (value, duration = 3250) => setTimeout(() => {
            set(value);
            setTimeout(() => set(""), duration);
        }, 75),
    };
};
export const flash = createFlash();