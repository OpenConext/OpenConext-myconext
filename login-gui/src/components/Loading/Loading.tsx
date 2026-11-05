import { Spinner } from '@surfnet/curve-react';

import { LoginContainer } from '../LoginContainer/LoginContainer';
import styles from './Loading.module.scss';

interface LoadingProps {
    title: string;
}

export const Loading = ({ title }: LoadingProps) => (
    <LoginContainer title={title}>
        <div className={styles.wrapper}>
            <Spinner className={styles.spinner} />
        </div>
    </LoginContainer>
);
