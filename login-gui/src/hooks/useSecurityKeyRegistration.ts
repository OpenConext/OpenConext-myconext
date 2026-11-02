import { useNavigate } from 'react-router';

import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import {
    InvalidRegistrationTokenError,
    type SecurityKeyRegistration,
    configQueryOptions,
    registerSecurityKey,
} from '../api';

export const useSecurityKeyRegistration = (token: string, name: string) => {
    const navigate = useNavigate();
    const queryClient = useQueryClient();

    // Fire the config request on mount, in parallel with the user reading the
    // page, so the fallback redirect URL is already cached when needed.
    useQuery(configQueryOptions);

    const handleSuccessfulRegistration = async (
        securityKeyRegistration: SecurityKeyRegistration,
    ) => {
        if (securityKeyRegistration.status === 'registered') {
            window.location.href = securityKeyRegistration.location;
            return;
        }
        const { myconextWebAuthnRedirectUrl } =
            await queryClient.ensureQueryData(configQueryOptions);
        if (!myconextWebAuthnRedirectUrl) {
            throw new Error('Missing myconextWebAuthnRedirectUrl');
        }
        window.location.href = myconextWebAuthnRedirectUrl;
    };

    return useMutation({
        mutationFn: () => registerSecurityKey(token, name),
        onSuccess: handleSuccessfulRegistration,
        onError: (error) => {
            if (error instanceof InvalidRegistrationTokenError) {
                navigate('/404', { replace: true });
            }
        },
    });
};
