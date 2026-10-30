import { Button } from '@surfnet/curve-react';

import I18n from '../../locale/I18n';
import styles from './AppFlow.module.scss';

const HELP_URL = 'https://eduid.nl/help';

type UseAppFooterProps = {
    onOtherMethod: () => void;
};

export const UseAppFooter = ({ onOtherMethod }: UseAppFooterProps) => (
    <div className={styles.footer}>
        <span>{I18n.t('useApp.noAccess')}</span>
        <a href={HELP_URL} target="_blank" rel="noopener noreferrer">
            {I18n.t('useApp.reRegister')}
        </a>
        <span>{I18n.t('useApp.or')}</span>
        <Button
            variant="link"
            type="button"
            className={`${styles.link} ${styles.lowercase}`}
            onClick={onOtherMethod}
        >
            {I18n.t('useApp.otherMethod')}
        </Button>
    </div>
);
