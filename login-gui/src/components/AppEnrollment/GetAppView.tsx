import { StoreBadges } from '@myconext/shared-gui';
import { Button } from '@surfnet/curve-react';

import I18n from '../../locale/I18n';
import { LoginContainer } from '../LoginContainer/LoginContainer';
import styles from './AppEnrollment.module.scss';

interface GetAppViewProps {
    onBack: () => void;
    onNext: () => void;
}

export const GetAppView = ({ onBack, onNext }: GetAppViewProps) => (
    <LoginContainer
        title={I18n.t('getApp.title')}
        description={I18n.t('getApp.description')}
        headerAlign="start"
    >
        <StoreBadges
            googleUrl={I18n.t('getApp.google')}
            appleUrl={I18n.t('getApp.apple')}
            googleLabel={I18n.t('getApp.googleLabel')}
            appleLabel={I18n.t('getApp.appleLabel')}
        />
        <p className={styles.after}>{I18n.t('getApp.after')}</p>
        <div className={styles.actions}>
            <Button type="button" variant="outline" onClick={onBack}>
                {I18n.t('getApp.back')}
            </Button>
            <Button
                type="button"
                className={styles.primary}
                onClick={onNext}
                autoFocus
            >
                {I18n.t('getApp.next')}
            </Button>
        </div>
    </LoginContainer>
);
