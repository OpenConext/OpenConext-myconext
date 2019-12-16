import {writable} from 'svelte/store';

export const user = writable({
    id: "",
    email: "jdoe@example.com",
    givenName: "John",
    familyName: "Doe",
    guest: true,
    usePassword: false
});

export const config = writable({
    loginUrl: "",
    baseDomain: ""
});

export const redirectPath = writable("");

export const flash = writable("");