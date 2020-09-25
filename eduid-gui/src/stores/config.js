import {writable} from 'svelte/store';

export const config = writable({
    eduIDDoLoginUrl: "",
    eduIDRegisterUrl: "",
    domain: ""
});
