import {writable} from 'svelte/store';

export const domains = writable({
    institutionDomainNames: [],
    institutionDomainNameWarning: false,

    allowedDomainNames: [],
    allowedDomainNamesError: false
});