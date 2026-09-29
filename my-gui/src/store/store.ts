import { create } from 'zustand';

interface DarkModeState {
    dark: boolean;
    toggleDark: () => void;
}

export const useDarkModeStore = create<DarkModeState>((set) => ({
    dark: false,
    toggleDark: () => set((state) => ({ dark: !state.dark })),
}));
