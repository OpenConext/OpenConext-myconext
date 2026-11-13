import { Spinner as CurveSpinner } from '@surfnet/curve-react';

import I18n from '../../locale/I18n';
import styles from './Loading.module.scss';

export const LoadingPage = () => (
    <div className={styles.page}>
        <CurveSpinner
            className={styles.spinner}
            aria-label={I18n.t('common.loading')}
        />
    </div>
);
