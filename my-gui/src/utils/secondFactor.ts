import type { User } from '../api';

// the eduID app is the second factor, destructive actions ask for it
export const hasSecondFactor = (user: User): boolean =>
    (user.loginOptions ?? []).includes('useApp');
