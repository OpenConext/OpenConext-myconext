import {writable} from 'svelte/store';

export const user = writable({
    email: "",
    givenName: "",
    familyName: "",
    password: "",
    rememberMe: false,
    usePassword: false,
    useWebAuth: false,
    createAccount: false,
    preferredLogin: "app",
    knownUser: null
});
