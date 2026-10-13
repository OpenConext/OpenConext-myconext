import type { ReactNode } from 'react';

import {
    Card,
    CardContent,
    CardDescription,
    CardHeader,
    CardTitle,
} from '@surfnet/curve-react';

import styles from './LoginContainer.module.scss';

interface LoginContainerProps {
    title: string;
    description?: string;
    children: ReactNode;
    footer?: ReactNode;
}

export const LoginContainer = ({
    title,
    description,
    children,
    footer,
}: LoginContainerProps) => (
    <div className={styles.container}>
        <Card className={styles.card}>
            <CardHeader className={styles.header}>
                <CardTitle className={styles.title}>
                    <h1>{title}</h1>
                </CardTitle>
                {description && (
                    <CardDescription className={styles.description}>
                        {description}
                    </CardDescription>
                )}
            </CardHeader>

            <CardContent className={styles.content}>{children}</CardContent>

            {footer && <div className={styles.footer}>{footer}</div>}
        </Card>
    </div>
);
