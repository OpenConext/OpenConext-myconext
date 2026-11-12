import type { ComponentProps, ReactNode } from 'react';

import { WarningDiamondIcon } from '@phosphor-icons/react';
import { Alert, AlertDescription } from '@surfnet/curve-react';

type ErrorAlertProps = {
    children: ReactNode;
    variant?: ComponentProps<typeof Alert>['variant'];
    className?: string;
};

export const ErrorAlert = ({
    children,
    variant = 'warning',
    className,
}: ErrorAlertProps) => (
    <Alert variant={variant} className={className}>
        <WarningDiamondIcon aria-hidden />
        <AlertDescription>{children}</AlertDescription>
    </Alert>
);
