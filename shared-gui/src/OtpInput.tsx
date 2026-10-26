import { InputOTP, InputOTPGroup, InputOTPSlot } from '@surfnet/curve-react';

import './OtpInput.scss';

const OTP_LENGTH = 6;

export type OtpInputProps = {
    value: string;
    onChange: (value: string) => void;
    onComplete: (value: string) => void;
    ariaLabel: string;
    disabled?: boolean;
};

export const OtpInput = ({
    value,
    onChange,
    onComplete,
    ariaLabel,
    disabled = false,
}: OtpInputProps) => (
    <InputOTP
        autoFocus
        maxLength={OTP_LENGTH}
        pattern="^\d+$"
        inputMode="numeric"
        autoComplete="one-time-code"
        aria-label={ariaLabel}
        value={value}
        disabled={disabled}
        containerClassName="shared-otp"
        onChange={onChange}
        onComplete={onComplete}
    >
        <InputOTPGroup>
            {Array.from({ length: OTP_LENGTH }, (_, index) => (
                <InputOTPSlot key={index} index={index} />
            ))}
        </InputOTPGroup>
    </InputOTP>
);
