import { create } from 'zustand';
import { persist } from 'zustand/middleware';

type AppStoreState = {
    dark: boolean;
    toggleDark: () => void;
    introHidden: boolean;
    hideIntro: () => void;
};

export const useAppStore = create<AppStoreState>()(
    persist(
        (set) => ({
            dark: false,
            toggleDark: () => set((state) => ({ dark: !state.dark })),
            introHidden: false,
            hideIntro: () => set({ introHidden: true }),
        }),
        {
            name: 'my-gui',
            // only the dismissed intro alert is remembered in localStorage
            partialize: (state) => ({ introHidden: state.introHidden }),
        },
    ),
);
