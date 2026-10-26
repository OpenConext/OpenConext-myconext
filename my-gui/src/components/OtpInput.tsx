import type { Ref } from 'react';

import { InputOTP, InputOTPGroup, InputOTPSlot } from '@surfnet/curve-react';

import { CODE_LENGTH } from '../hooks/useCodeVerification';

type OtpInputProps = {
    ref: Ref<HTMLInputElement>;
    value: string;
    disabled: boolean;
    ariaLabel: string;
    testId: string;
    containerClassName: string;
    slotsClassName: string;
    onChange: (value: string) => void;
    onComplete: (value: string) => void;
};

export const OtpInput = ({
    ref,
    value,
    disabled,
    ariaLabel,
    testId,
    containerClassName,
    slotsClassName,
    onChange,
    onComplete,
}: OtpInputProps) => (
    <InputOTP
        ref={ref}
        autoFocus
        data-testid={testId}
        maxLength={CODE_LENGTH}
        pattern="^\d+$"
        inputMode="numeric"
        autoComplete="one-time-code"
        aria-label={ariaLabel}
        value={value}
        disabled={disabled}
        containerClassName={containerClassName}
        onChange={onChange}
        onComplete={onComplete}
    >
        <InputOTPGroup className={slotsClassName}>
            {Array.from({ length: CODE_LENGTH }, (_, index) => (
                <InputOTPSlot key={index} index={index} />
            ))}
        </InputOTPGroup>
    </InputOTP>
);
