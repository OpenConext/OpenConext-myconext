import { Navigate, useLocation, useNavigate, useParams } from 'react-router';

import { get } from '@github/webauthn-json';
import { Button } from '@surfnet/curve-react';
import { useMutation, useQuery } from '@tanstack/react-query';

import {
    fetchServiceName,
    webAuthnFinishAuthentication,
    webAuthnStartAuthentication,
} from '../../api';
import { ErrorAlert, LoadingButton, LoginContainer } from '../../components';
import I18n from '../../locale/I18n';
import { rememberLoginMethod } from '../../utils/loginMethods';
import styles from './route.module.scss';

export default function UseWebAuthn() {
    const { id = '' } = useParams();
    const { state } = useLocation();
    const navigate = useNavigate();
    const email: string = state?.email ?? '';

    const { data: service } = useQuery({
        queryKey: ['serviceName', id],
        queryFn: () => fetchServiceName(id),
        enabled: id !== '',
    });

    const {
        mutate: startAuthentication,
        isPending,
        isSuccess,
        isError,
    } = useMutation({
        mutationFn: async () => {
            const { publicKeyCredentialRequestOptions } =
                await webAuthnStartAuthentication(email, id);
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
            rememberLoginMethod('useWebAuthn');
            window.location.href = url;
        },
    });

    if (!email) {
        return <Navigate to={`/login/${id}`} replace />;
    }

    const goToOptions = () => navigate(`/options/${id}`, { state: { email } });

    return (
        <LoginContainer
            title={I18n.t('webAuthn.title')}
            description={
                service?.name
                    ? I18n.t('webAuthn.descriptionWithService', {
                          serviceName: service.name,
                      })
                    : I18n.t('webAuthn.description')
            }
            footer={
                <Button
                    variant="link"
                    type="button"
                    className={styles.link}
                    onClick={goToOptions}
                >
                    {I18n.t('webAuthn.otherMethod')}
                </Button>
            }
            onBack={goToOptions}
            backLabel={email}
        >
            <div className={styles.content}>
                <p>{I18n.t('webAuthn.explanation')}</p>
                {isError && <ErrorAlert>{I18n.t('webAuthn.error')}</ErrorAlert>}
                <LoadingButton
                    type="button"
                    loading={isPending}
                    disabled={isSuccess}
                    autoFocus
                    onClick={() => startAuthentication()}
                >
                    {I18n.t('webAuthn.submit')}
                </LoadingButton>
            </div>
        </LoginContainer>
    );
}
