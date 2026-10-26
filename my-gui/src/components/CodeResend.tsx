import { Button } from '@surfnet/curve-react';

type CodeResendProps = {
    className: string;
    linkClassName: string;
    resent: boolean;
    canResend: boolean;
    pending: boolean;
    resentLabel: string;
    promptLabel: string;
    resendLabel: string;
    onResend: () => void;
};

export const CodeResend = ({
    className,
    linkClassName,
    resent,
    canResend,
    pending,
    resentLabel,
    promptLabel,
    resendLabel,
    onResend,
}: CodeResendProps) => (
    <p className={className}>
        {resent ? resentLabel : null}{' '}
        {canResend && (
            <>
                {promptLabel}{' '}
                <Button
                    type="button"
                    variant="link"
                    className={linkClassName}
                    disabled={pending}
                    onClick={onResend}
                >
                    {resendLabel}
                </Button>
            </>
        )}
    </p>
);
