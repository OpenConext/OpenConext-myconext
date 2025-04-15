import {writable} from 'svelte/store';

export const user = writable({
    email: "",
    givenName: "",
    familyName: "",
    password: "",
    phoneNumber: "",
    rememberMe: false,
    usePassword: false,
    useWebAuth: false,
    createAccount: false,
    preferredLogin: null,
    knownUser: null,
    delay: 0
});
