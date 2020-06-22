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
    usePublicKey: false,
    rememberMe: true,
    created: 0,
    publicKeyCredentials: {},
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
    featureOidcTokenAPI: false
});

export const redirectPath = writable("");

export const duplicatedEmail = writable("");

const createFlash = () => {
    const {subscribe, set} = writable("");

    return {
        subscribe,
        setValue: value => setTimeout(() => {
            set(value);
            setTimeout(() => set(""), 3000)
        }, 125),
    };
};
export const flash = createFlash();