import { create } from 'zustand';

type AppStoreState = {
    dark: boolean;
    toggleDark: () => void;
};

export const useAppStore = create<AppStoreState>((set) => ({
    dark: false,
    toggleDark: () => set((state) => ({ dark: !state.dark })),
}));
