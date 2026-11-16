import { useState } from 'react';

import { InfoIcon } from '@phosphor-icons/react';
import {
    Alert,
    AlertDescription,
    AlertTitle,
    Button,
} from '@surfnet/curve-react';

import I18n, { tArray } from '../../locale/I18n.ts';
import { Spinner } from '../Loading/Spinner';
import { LoginContainer } from '../LoginContainer/LoginContainer';
import styles from './ServiceDeskView.module.scss';

interface ServiceDeskViewProps {
    serviceName: string;
    url: string;
    onBack: () => void;
}

export const ServiceDeskView = ({
    serviceName,
    url,
    onBack,
}: ServiceDeskViewProps) => {
    const [redirecting, setRedirecting] = useState(false);

    const proceed = () => {
        setRedirecting(true);
        window.location.href = url;
    };

    return (
        <LoginContainer
            title={I18n.t('stepup.serviceDesk.title')}
            description={I18n.t('stepup.serviceDesk.description')}
            headerAlign="start"
            onBack={redirecting ? undefined : onBack}
            backLabel={I18n.t('stepup.proveIdentity.back')}
        >
            <div className={styles.steps}>
                <p className={styles.stepsHeader}>
                    {I18n.t('stepup.serviceDesk.stepsHeader')}
                </p>
                <ol className={styles.list}>
                    {tArray('stepup.serviceDesk.steps', (step, index) => (
                        <li key={index}>{step}</li>
                    ))}
                </ol>
            </div>
            <Alert>
                <InfoIcon aria-hidden />
                <AlertTitle>
                    {I18n.t('stepup.serviceDesk.alertTitle')}
                </AlertTitle>
                <AlertDescription>
                    {I18n.t('stepup.serviceDesk.redirectWarning', {
                        serviceName,
                    })}
                </AlertDescription>
            </Alert>
            {redirecting ? (
                <Spinner label={I18n.t('stepup.serviceDesk.title')} />
            ) : (
                <Button type="button" onClick={proceed} autoFocus>
                    {I18n.t('stepup.serviceDesk.next')}
                </Button>
            )}
        </LoginContainer>
    );
};
