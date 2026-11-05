import { useState } from 'react';

import { ShieldIcon } from '@phosphor-icons/react';
import {
    Alert,
    AlertDescription,
    AlertDialog,
    AlertDialogAction,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogFooter,
    AlertDialogHeader,
    AlertDialogTitle,
    AlertTitle,
    Button,
} from '@surfnet/curve-react';

import mobileApp from '../../assets/mobile-app.svg';
import I18n from '../../locale/I18n';
import { LoginContainer } from '../LoginContainer/LoginContainer';
import styles from './AppRequiredView.module.scss';

interface AppRequiredViewProps {
    serviceName: string;
    onDownload: () => void;
    onRefuse: () => void;
}

export const AppRequiredView = ({
    serviceName,
    onDownload,
    onRefuse,
}: AppRequiredViewProps) => {
    const [confirmingRefuse, setConfirmingRefuse] = useState(false);

    return (
        <LoginContainer
            title={I18n.t('appRequired.title')}
            description={I18n.t('appRequired.description', { serviceName })}
            headerAlign="start"
        >
            <Alert>
                <ShieldIcon aria-hidden />
                <AlertTitle>{I18n.t('appRequired.alertTitle')}</AlertTitle>
                <AlertDescription>
                    {I18n.t('appRequired.alertDescription')}
                </AlertDescription>
            </Alert>
            <img className={styles.image} src={mobileApp} alt="" />
            <div className={styles.actions}>
                <Button
                    type="button"
                    variant="outline"
                    onClick={() => setConfirmingRefuse(true)}
                >
                    {I18n.t('appRequired.refuse')}
                </Button>
                <Button
                    type="button"
                    className={styles.download}
                    onClick={onDownload}
                    autoFocus
                >
                    {I18n.t('appRequired.download')}
                </Button>
            </div>
            <AlertDialog
                open={confirmingRefuse}
                onOpenChange={setConfirmingRefuse}
            >
                <AlertDialogContent>
                    <AlertDialogHeader>
                        <AlertDialogTitle>
                            {I18n.t('appRequired.warningTitle')}
                        </AlertDialogTitle>
                        <AlertDialogDescription>
                            {I18n.t('appRequired.warning', { serviceName })}
                        </AlertDialogDescription>
                    </AlertDialogHeader>
                    <AlertDialogFooter>
                        <Button
                            type="button"
                            variant="outline"
                            onClick={onRefuse}
                        >
                            {I18n.t('appRequired.confirmRefuse')}
                        </Button>
                        <AlertDialogAction onClick={onDownload} autoFocus>
                            {I18n.t('appRequired.download')}
                        </AlertDialogAction>
                    </AlertDialogFooter>
                </AlertDialogContent>
            </AlertDialog>
        </LoginContainer>
    );
};
