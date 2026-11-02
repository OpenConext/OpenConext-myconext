import { Navigate, useNavigate, useSearchParams } from 'react-router';

import { create } from '@github/webauthn-json';
import type { PublicKeyCredentialWithAttestationJSON } from '@github/webauthn-json';
import { WarningDiamondIcon } from '@phosphor-icons/react';
import { Alert, AlertDescription, Button } from '@surfnet/curve-react';
import { useMutation, useQueryClient } from '@tanstack/react-query';

import {
    fetchConfig,
    webAuthnFinishRegistration,
    webAuthnStartRegistration,
} from '../../api';
import type { WebAuthnRegistrationOptions } from '../../api';
import { LoginContainer } from '../../components';
import I18n from '../../locale/I18n';
import styles from './route.module.scss';

type RegistrationResult =
    | { status: 'invalidToken' }
    | { status: 'redirect'; url: string };

export default function WebAuthn() {
    const navigate = useNavigate();
    const queryClient = useQueryClient();
    const [searchParams] = useSearchParams();
    const token = searchParams.get('token') ?? '';
    const name = searchParams.get('name') ?? '';

    const {
        mutate: startRegistration,
        isPending,
        isSuccess,
        isError,
    } = useMutation<RegistrationResult>({
        mutationFn: async () => {
            let request: WebAuthnRegistrationOptions;
            try {
                request = await webAuthnStartRegistration(token);
            } catch {
                return { status: 'invalidToken' };
            }

            let credentials: PublicKeyCredentialWithAttestationJSON;
            try {
                credentials = await create({ publicKey: request });
            } catch {
                // Browser rejects creation when the key is already registered
                const config = await queryClient.ensureQueryData({
                    queryKey: ['config'],
                    queryFn: fetchConfig,
                });
                if (!config.myconextWebAuthnRedirectUrl) {
                    throw new Error('Missing myconextWebAuthnRedirectUrl');
                }
                return {
                    status: 'redirect',
                    url: config.myconextWebAuthnRedirectUrl,
                };
            }

            const { location } = await webAuthnFinishRegistration(
                token,
                name,
                // rawId is not supported server-side
                JSON.stringify({ ...credentials, rawId: undefined }),
                request,
            );
            return { status: 'redirect', url: location };
        },
        onSuccess: (result) => {
            if (result.status === 'invalidToken') {
                navigate('/404', { replace: true });
            } else {
                window.location.href = result.url;
            }
        },
    });

    if (!token) {
        return <Navigate to="/404" replace />;
    }

    return (
        <LoginContainer title={I18n.t('addWebAuthn.title')}>
            <div className={styles.content}>
                <p>{I18n.t('addWebAuthn.explanation')}</p>
                {isError && (
                    <Alert variant="warning">
                        <WarningDiamondIcon />
                        <AlertDescription>
                            {I18n.t('addWebAuthn.error')}
                        </AlertDescription>
                    </Alert>
                )}
                <Button
                    type="button"
                    disabled={isPending || isSuccess}
                    autoFocus
                    onClick={() => startRegistration()}
                >
                    {I18n.t('addWebAuthn.submit')}
                </Button>
            </div>
        </LoginContainer>
    );
}
