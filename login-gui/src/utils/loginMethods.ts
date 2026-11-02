import Cookies from 'js-cookie';

export const LOGIN_PREFERENCE_COOKIE = 'login_preference';
export const USERNAME_COOKIE = 'username';

export type LoginMethod = 'useApp' | 'useWebAuthn' | 'useCode' | 'usePassword';

const METHOD_ROUTES: Record<LoginMethod, string> = {
    useApp: 'useapp',
    useWebAuthn: 'usewebauthn',
    useCode: 'code',
    usePassword: 'usepassword',
};

export const rememberLoginMethod = (method: LoginMethod): void => {
    Cookies.set(LOGIN_PREFERENCE_COOKIE, method, {
        expires: 365,
        secure: true,
        sameSite: 'Lax',
    });
};

export const isLoginMethod = (key: string): key is LoginMethod =>
    key in METHOD_ROUTES;

export const loginMethodPath = (method: LoginMethod, id: string): string =>
    `/${METHOD_ROUTES[method]}/${id}`;

/**
 * Picks the method to start with. By contract the server orders the list from
 * most to least secure, so the first entry is the fallback. A remembered
 * preference is only honoured when the server still offers it.
 */
export const pickLoginMethod = (
    methods: string[],
    magicLink: boolean,
    mfaRequired: boolean,
): LoginMethod | undefined => {
    const available = methods.filter(isLoginMethod);

    if (magicLink && available.includes('useCode')) {
        return 'useCode';
    }

    if (mfaRequired && available.includes('useApp')) {
        return 'useApp';
    }

    const preferred = Cookies.get(LOGIN_PREFERENCE_COOKIE);

    if (
        preferred &&
        isLoginMethod(preferred) &&
        available.includes(preferred)
    ) {
        return preferred;
    }

    return available[0];
};

export const getKnownUser = (): string | undefined =>
    Cookies.get(USERNAME_COOKIE) || undefined;

export const rememberKnownUser = (email: string): void => {
    Cookies.set(USERNAME_COOKIE, email, {
        expires: 365,
        secure: true,
        sameSite: 'Lax',
    });
};

export const forgetKnownUser = (): void => {
    Cookies.remove(USERNAME_COOKIE);
};
