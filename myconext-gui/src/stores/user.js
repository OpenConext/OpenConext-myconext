import {writable} from 'svelte/store';

export const user = writable({
    id: "",
    email: "",
    givenName: "",
    familyName: "",
    guest: true,
    schacHomeOrganization: "",
    uid: "",
    usePassword: false,
    rememberMe: true
});

export const config = writable({
    loginUrl: "",
    baseDomain: "",
    migrationLandingPageUrl: "",
    myConextUrlGuestIdp: ""
});

export const redirectPath = writable("");

const createFlash = () => {
    const {subscribe, set, update} = writable("");

    return {
        subscribe,
        setValue: value => {
            setTimeout(() => {
                set(value);
                setTimeout(() => {
                    set(" ");
                    setTimeout(() => set(""), 850)
                }, 3000)
            }, 85);

        },

    };
};
export const flash = createFlash();