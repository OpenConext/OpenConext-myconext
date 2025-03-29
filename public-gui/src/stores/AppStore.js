import {create} from 'zustand'

export const useAppStore = create(set => ({
    config: {
        idpBaseUrl: "http://localhost:3000",
        spBaseUrl: "http://localhost:3000"
    },
}));
