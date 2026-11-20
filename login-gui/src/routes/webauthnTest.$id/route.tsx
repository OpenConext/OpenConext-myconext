import { Navigate, useParams, useSearchParams } from 'react-router';

import { get } from '@github/webauthn-json';
import { useMutation } from '@tanstack/react-query';

import {
    webAuthnFinishAuthentication,
    webAuthnStartAuthentication,
} from '../../api';
import { ErrorAlert, LoadingButton, LoginContainer } from '../../components';
import I18n from '../../locale/I18n';
import styles from './route.module.scss';

// Entered from the "test security key" action in the profile. It must not
// remember the login method, as it is not a real login.
export default function WebAuthnTest() {
    const { id = '' } = useParams();
    const [searchParams] = useSearchParams();
    const email = searchParams.get('email') ?? '';

    const {
        mutate: startTest,
        isPending,
        isSuccess,
        isError,
    } = useMutation({
        mutationFn: async () => {
            const { publicKeyCredentialRequestOptions } =
                await webAuthnStartAuthentication(email, id, true);
            const credentials = await get({
                publicKey: publicKeyCredentialRequestOptions,
            });
            return webAuthnFinishAuthentication(
                // rawId is not supported server-side
                JSON.stringify({ ...credentials, rawId: undefined }),
                id,
            );
        },
        onSuccess: ({ url }) => {
            window.location.href = url;
        },
    });

    if (!email) {
        return <Navigate to="/expired" replace />;
    }

    return (
        <LoginContainer title={I18n.t('webAuthnTest.title')}>
            <div className={styles.content}>
                <p>{I18n.t('webAuthnTest.explanation')}</p>
                {isError && (
                    <ErrorAlert>{I18n.t('webAuthnTest.error')}</ErrorAlert>
                )}
                <LoadingButton
                    type="button"
                    loading={isPending || isSuccess}
                    autoFocus
                    data-testid="webauthn-test-submit"
                    onClick={() => startTest()}
                >
                    {I18n.t('webAuthnTest.submit')}
                </LoadingButton>
            </div>
        </LoginContainer>
    );
}
