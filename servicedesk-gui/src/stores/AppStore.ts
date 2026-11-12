import { create } from 'zustand';

export type User = {
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
};

export type FlashType = 'info' | 'warning' | 'error';

export type Flash = {
    msg?: string;
    type?: FlashType;
    className?: string;
    action?: () => void;
    actionLabel?: string;
};

type AppState = {
    user: User;
    config: Config;
    controlCode: ControlCode;
    csrfToken?: string;
    flash: Flash;
    setFlash: (message: string, type?: FlashType) => void;
    clearFlash: () => void;
};

export const useAppStore = create<AppState>((set) => ({
    user: {},
    config: {},
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
