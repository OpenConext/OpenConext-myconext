import { Navigate, useSearchParams } from 'react-router';

import { WarningDiamondIcon } from '@phosphor-icons/react';
import { Alert, AlertDescription, Button } from '@surfnet/curve-react';

import { LoginContainer } from '../../components';
import { useSecurityKeyRegistration } from '../../hooks';
import I18n from '../../locale/I18n';
import styles from './route.module.scss';

export default function WebAuthn() {
    const [searchParams] = useSearchParams();
    const token = searchParams.get('token') ?? '';
    const name = searchParams.get('name') ?? '';

    const {
        mutate: startRegistration,
        isPending,
        isSuccess,
        isError,
    } = useSecurityKeyRegistration(token, name);

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
