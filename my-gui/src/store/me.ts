import { create } from 'zustand';

import { fetchMe } from '../api/me';
import type { User } from '../api/me';

type UserStoreState = {
    user: User | undefined;
    isError: boolean;
};

export const useUserStore = create<UserStoreState>()(() => ({
    user: undefined,
    isError: false,
}));

let pending: Promise<void> | undefined;

// fetches the user from the server, only the first call does a request
export const loadUser = () => {
    pending ??= refreshUser();
    return pending;
};

// re-fetches the user after something changed server-side and updates the store
export const refreshUser = async () => {
    try {
        useUserStore.setState({ user: await fetchMe(), isError: false });
    } catch {
        useUserStore.setState({ isError: true });
    }
};

// for mutations that already return the updated user
export const setUser = (user: User) =>
    useUserStore.setState({ user, isError: false });
