import {writable} from 'svelte/store';

export const config = writable({
    eduIDLoginUrl: "",
    eduIDRegisterUrl: ""
});
