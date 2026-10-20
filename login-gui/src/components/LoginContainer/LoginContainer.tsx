import type { ReactNode } from 'react';

import { ArrowLeftIcon } from '@phosphor-icons/react';
import {
    Button,
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
    onBack?: () => void;
    backLabel?: string;
}

export const LoginContainer = ({
    title,
    description,
    children,
    footer,
    onBack,
    backLabel,
}: LoginContainerProps) => (
    <div className={styles.container}>
        <div className={styles.inner}>
            <div className={styles.top} data-hidden={!onBack || undefined}>
                <Button
                    type="button"
                    variant="ghost"
                    size="sm"
                    className={styles.back}
                    onClick={onBack}
                >
                    <ArrowLeftIcon aria-hidden data-icon="inline-start" />
                    {backLabel}
                </Button>
            </div>
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
    </div>
);
