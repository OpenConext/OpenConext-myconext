import { Button } from '@surfnet/curve-react';

import I18n from '../../locale/I18n';
import styles from './AppFlow.module.scss';

export const TimedOut = () => (
    <div className={styles.content}>
        <p>{I18n.t('useApp.timeOutInfo')}</p>
        <Button type="button" onClick={() => window.location.reload()}>
            {I18n.t('useApp.timeOutRetry')}
        </Button>
    </div>
);
