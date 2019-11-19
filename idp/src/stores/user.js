import {writable} from 'svelte/store';

export const user = writable({
    email: "nice@qwert.com",
    givenName: "",
    familyName: "",
    rememberMe: false,
    usePassword: false,
    createAccount: false
});