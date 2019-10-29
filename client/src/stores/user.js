import {writable} from 'svelte/store';

export const user = writable({
    givenName: "",
    familyName: "",
    email: ""
});