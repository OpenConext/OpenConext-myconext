import { create } from 'zustand';

import { type Config, fetchConfig } from '../api/config';

type ConfigStoreState = {
    config: Config | undefined;
    isError: boolean;
    // fetches the config from the server, only the first call does a request
    loadConfig: () => Promise<void>;
};

let pending: Promise<void> | undefined;

export const useConfigStore = create<ConfigStoreState>()((set) => ({
    config: undefined,
    isError: false,
    loadConfig: () => {
        pending ??= (async () => {
            try {
                set({ config: await fetchConfig(), isError: false });
            } catch {
                set({ isError: true });
            }
        })();
        return pending;
    },
}));
