import type { Ref } from 'react';

import { InputOTP, InputOTPGroup, InputOTPSlot } from '@surfnet/curve-react';

import styles from './OtpInput.module.scss';

const DEFAULT_OTP_LENGTH = 6;

export type OtpInputProps = {
    value: string;
    onChange: (value: string) => void;
    onComplete: (value: string) => void;
    ariaLabel: string;
    disabled?: boolean;
    ref?: Ref<HTMLInputElement>;
    testId?: string;
    length?: number;
    // regular expression the characters have to match, digits only by default
    pattern?: string;
    autoFocus?: boolean;
    // alignment of the slots in their container, centered by default
    align?: 'center' | 'start';
};

export const OtpInput = ({
    value,
    onChange,
    onComplete,
    ariaLabel,
    disabled = false,
    ref,
    testId,
    length = DEFAULT_OTP_LENGTH,
    pattern = '^\\d+$',
    autoFocus = true,
    align = 'center',
}: OtpInputProps) => (
    <InputOTP
        ref={ref}
        autoFocus={autoFocus}
        data-testid={testId}
        maxLength={length}
        pattern={pattern}
        inputMode={pattern === '^\\d+$' ? 'numeric' : 'text'}
        autoComplete="one-time-code"
        aria-label={ariaLabel}
        value={value}
        disabled={disabled}
        containerClassName={`${styles.otp} ${align === 'start' ? styles.start : ''}`}
        onChange={onChange}
        onComplete={onComplete}
    >
        <InputOTPGroup>
            {Array.from({ length }, (_, index) => (
                <InputOTPSlot key={index} index={index} />
            ))}
        </InputOTPGroup>
    </InputOTP>
);
