import { Spinner as CurveSpinner } from '@surfnet/curve-react';

import I18n from '../../locale/I18n';
import styles from './Loading.module.scss';

interface LoadingSpinnerProps {
    label?: string;
}

export const Spinner = ({
    label = I18n.t('common.loading'),
}: LoadingSpinnerProps) => (
    <div className={styles.wrapper}>
        <CurveSpinner className={styles.spinner} aria-label={label} />
    </div>
);
