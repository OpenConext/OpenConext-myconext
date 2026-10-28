import { apiFetch } from '../utils/apiFetch.ts';

export type NewUser = {
    email: string;
    givenName: string;
    familyName: string;
};

export type GenerateCodeNewUserResponse = {
    stepup?: boolean;
    explanation?: string;
};

export const generateCodeNewUser = async (
    user: NewUser,
    authenticationRequestId: string,
    captchaResponse?: string | null,
) =>
    apiFetch<GenerateCodeNewUserResponse>(
        '/myconext/api/idp/generate_code_request',
        {
            method: 'POST',
            body: { user, authenticationRequestId, captchaResponse },
        },
    );
