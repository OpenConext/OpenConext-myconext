import type { ReactNode } from 'react';

import { WarningDiamondIcon } from '@phosphor-icons/react';
import { Alert, AlertDescription } from '@surfnet/curve-react';

// Copy of `AlertVariantName` from `packages/contracts/src/alert.ts` in curve (@surfnet/curve-contracts).
// Needed because curve-react@0.10.0 lists curve-contracts only as a devDependency, so the type
// resolves to `any` here. Remove once curve ships resolvable types.
type AlertVariant = 'default' | 'info' | 'success' | 'warning' | 'danger';

type ErrorAlertProps = {
    children: ReactNode;
    variant?: AlertVariant;
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
