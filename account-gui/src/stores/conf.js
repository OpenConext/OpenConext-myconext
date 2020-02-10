import {writable} from 'svelte/store';

export const conf = writable({
    migrationUrl: "",
    magicLinkUrl: ""
});