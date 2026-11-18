import { handleArrowKeyNavigation } from '@myconext/shared-gui';
import { Button } from '@surfnet/curve-react';

import mobileApp from '../../assets/mobile-app.svg';
import I18n from '../../locale/I18n';
import { LoginContainer } from '../LoginContainer/LoginContainer';
import styles from './NudgeAppView.module.scss';

interface NudgeAppViewProps {
    onDownload: () => void;
    onSkip: () => void;
}

export const NudgeAppView = ({ onDownload, onSkip }: NudgeAppViewProps) => (
    <LoginContainer
        title={I18n.t('nudgeApp.title')}
        description={I18n.t('nudgeApp.description')}
        headerAlign="start"
    >
        <img className={styles.image} src={mobileApp} alt="" />
        <div className={styles.actions} onKeyDown={handleArrowKeyNavigation}>
            <Button
                type="button"
                variant="outline"
                onClick={onSkip}
                data-testid="nudge-app-skip"
            >
                {I18n.t('nudgeApp.skip')}
            </Button>
            <Button
                type="button"
                data-testid="nudge-app-download"
                className={styles.download}
                onClick={onDownload}
                autoFocus
            >
                {I18n.t('nudgeApp.download')}
            </Button>
        </div>
    </LoginContainer>
);
