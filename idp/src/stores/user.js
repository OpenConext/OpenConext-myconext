import {writable} from 'svelte/store';

export const user = writable({
    email: "",
    givenName: "",
    familyName: "",
    rememberMe: false
});