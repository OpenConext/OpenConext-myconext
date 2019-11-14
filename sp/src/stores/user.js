import {writable} from 'svelte/store';

export const user = writable({
    id: "",
    email: "",
    givenName: "",
    familyName: "",
    guest: true
});

export const redirectPath = writable("");