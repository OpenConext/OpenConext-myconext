import {writable} from 'svelte/store';

export const user = writable({
    id: "",
    email: "",
    chosenName: "",
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
    externalLinkedAccounts: [],
    eduIdPerServiceProvider: {},
    oidcTokens: []
});

export const config = writable({
    loginUrl: "",
    baseDomain: "",
    myConextUrlGuestIdp: "",
    spBaseUrl: "",
    createEduIDInstitutionLanding: false,
    eduIDWebAuthnUrl: "",
    magicLinkUrl: "",
    domain: "",
    idpBaseUrl: "",
    featureWebAuthn: false,
    featureOidcTokenAPI: false,
    featureConnections: false,
    featureAllowList: false,
    featureDefaultRememberMe: false,
    featureWarningEducationalEmailDomain: false,
    featureIdVerify: false,
    featureServiceDeskActive: false,
    createEduIDInstitutionEnabled: false,
    expirationNonValidatedDurationDays: 0
});

export const redirectPath = writable("");

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