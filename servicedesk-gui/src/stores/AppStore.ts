import { create } from 'zustand';

import { configuration, me } from '../api';

export type User = {
    id?: string;
    displayName?: string;
    name?: string;
    schacHomeOrganization?: string;
    serviceDeskMember?: boolean;
};

export type Config = {
    isAuthenticated?: boolean;
    loginUrlServiceDesk?: string;
    accountBaseUrl?: string;
};

export type ControlCode = {
    code?: string;
    firstName?: string;
    lastName?: string;
    dayOfBirth?: string;
    userUid?: string;
    userId?: string;
};

export type FlashType = 'info' | 'warning' | 'error';

export type Flash = {
    msg?: string;
    type?: FlashType;
    className?: string;
    action?: () => void;
    actionLabel?: string;
};

export const AUTH_STATUS = {
    LOADING: 'loading',
    UNAUTHENTICATED: 'unauthenticated',
    FORBIDDEN: 'forbidden',
    AUTHORIZED: 'authorized',
} as const;

export type AuthStatus = (typeof AUTH_STATUS)[keyof typeof AUTH_STATUS];

const DARK_KEY = 'darkMode';

const darkQuery =
    typeof window !== 'undefined'
        ? window.matchMedia?.('(prefers-color-scheme: dark)')
        : undefined;

const storedDark = (): string | null => {
    try {
        return localStorage.getItem(DARK_KEY);
    } catch {
        return null;
    }
};

// An explicit choice wins, otherwise follow the OS setting
const readDark = () => {
    const stored = storedDark();
    return stored === null ? Boolean(darkQuery?.matches) : stored === 'true';
};

type AppState = {
    dark: boolean;
    toggleDark: () => void;
    user: User;
    config: Config;
    authStatus: AuthStatus;
    // Fetches /config and /me once; later calls reuse the same result
    loadSession: () => Promise<AuthStatus>;
    controlCode: ControlCode;
    csrfToken?: string;
    flash: Flash;
    setFlash: (message: string, type?: FlashType) => void;
    clearFlash: () => void;
};

// Shared by every caller so /config and /me are fetched only once per page load
let sessionPromise: Promise<AuthStatus> | undefined;

export const useAppStore = create<AppState>((set) => ({
    dark: readDark(),
    toggleDark: () =>
        set((state) => {
            const dark = !state.dark;
            try {
                localStorage.setItem(DARK_KEY, String(dark));
            } catch {
                // storage unavailable, the preference just isn't remembered
            }
            return { dark };
        }),
    user: {},
    config: {},
    authStatus: AUTH_STATUS.LOADING,
    loadSession: () => {
        sessionPromise ??= (async () => {
            let authStatus: AuthStatus;
            try {
                const config = await configuration();
                set({ config });
                if (config.isAuthenticated) {
                    const user = await me();
                    set({ user });
                    authStatus = user.serviceDeskMember
                        ? AUTH_STATUS.AUTHORIZED
                        : AUTH_STATUS.FORBIDDEN;
                } else {
                    authStatus = AUTH_STATUS.UNAUTHENTICATED;
                }
            } catch (error) {
                set({ authStatus: AUTH_STATUS.FORBIDDEN });
                throw error;
            }
            set({ authStatus });
            return authStatus;
        })();
        return sessionPromise;
    },
    controlCode: {},
    flash: { msg: '', className: 'hide', type: 'info' },
    setFlash: (message, type) => {
        set({ flash: { msg: message, type: type || 'info' } });
        if (!type || type === 'info') {
            setTimeout(() => set({ flash: {} }), 5000);
        }
    },
    clearFlash: () => set({ flash: {} }),
}));

// The store is the single source of truth, the class on <html> just mirrors it
if (typeof document !== 'undefined') {
    const applyDark = ({ dark }: { dark: boolean }) =>
        document.documentElement.classList.toggle('dark', dark);
    applyDark(useAppStore.getState());
    useAppStore.subscribe(applyDark);
}

// Follow live OS changes until the user has made an explicit choice
darkQuery?.addEventListener('change', (e) => {
    if (storedDark() === null) {
        useAppStore.setState({ dark: e.matches });
    }
});
