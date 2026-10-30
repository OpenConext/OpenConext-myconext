import { Button } from '@surfnet/curve-react';

import I18n from '../../locale/I18n';
import styles from './AppFlow.module.scss';

type PushPromptProps = {
    onShowQr: () => void;
};

export const PushPrompt = ({ onShowQr }: PushPromptProps) => (
    <div className={styles.help}>
        <span>{I18n.t('useApp.noNotification')}</span>
        <Button
            variant="link"
            type="button"
            className={styles.link}
            onClick={onShowQr}
        >
            {I18n.t('useApp.createQr')}
        </Button>
    </div>
);
