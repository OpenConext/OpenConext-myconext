import {writable} from 'svelte/store';

export const user = writable({
    id: "",
    email: "",
    givenName: "",
    familyName: "",
    guest: true,
    usePassword: false,
    rememberMe: true
});

export const config = writable({
    loginUrl: "",
    baseDomain: ""
});

export const redirectPath = writable("");

export const flash = writable("");