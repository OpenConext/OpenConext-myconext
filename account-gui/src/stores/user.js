import {writable} from 'svelte/store';

export const user = writable({
    email: "jdoe@example.com",
    givenName: "",
    familyName: "",
    password: "",
    rememberMe: false,
    usePassword: false,
    useWebAuth: false,
    createAccount: false,
    preferredLogin: null,
    knownUser: null
});
