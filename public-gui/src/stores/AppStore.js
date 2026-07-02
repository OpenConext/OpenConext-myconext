import {create} from 'zustand'

export const useAppStore = create(() => ({
    config: {
        accountBaseUrl: "http://localhost:3000",
        myconextBaseUrl: "http://localhost:3000"
    },
}));
