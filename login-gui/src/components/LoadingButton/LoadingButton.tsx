import type { ComponentProps } from 'react';

import { Button, Spinner } from '@surfnet/curve-react';

type LoadingButtonProps = ComponentProps<typeof Button> & {
    loading?: boolean;
};

export const LoadingButton = ({
    loading = false,
    disabled,
    children,
    ...props
}: LoadingButtonProps) => (
    <Button
        {...props}
        disabled={disabled || loading}
        aria-busy={loading || undefined}
    >
        {/* Spinner defaults to an English aria-label, so hide it and rely on the button label */}
        {loading && <Spinner aria-hidden />}
        {children}
    </Button>
);
