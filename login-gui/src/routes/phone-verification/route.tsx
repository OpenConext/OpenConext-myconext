import { useState } from 'react';
import {
    Navigate,
    useLocation,
    useNavigate,
    useSearchParams,
} from 'react-router';

import { useMutation } from '@tanstack/react-query';

import { type SendError, sendPhoneCode, toSendError } from '../../api';
import { PhoneFinalizedView, PhoneVerificationView } from '../../components';
import {
    type PhoneNavigationState,
    isValidPhoneNumber,
    normalizePhoneNumber,
} from '../../utils/phoneNumber';

export default function PhoneVerification() {
    const navigate = useNavigate();
    const { state } = useLocation() as { state: PhoneNavigationState };
    const [searchParams] = useSearchParams();
    const hash = searchParams.get('h') ?? '';
    const query = `?h=${encodeURIComponent(hash)}`;

    const [phoneNumber, setPhoneNumber] = useState<string>(
        state?.phoneNumber ?? '',
    );
    const [touched, setTouched] = useState(false);
    const [sendError, setSendError] = useState<SendError | null>(null);

    const {
        mutate: send,
        isPending: isSending,
        isSuccess,
    } = useMutation({
        mutationFn: (number: string) => sendPhoneCode(hash, number),
        onSuccess: (_, number) =>
            navigate(`/phone-confirmation${query}`, {
                state: { phoneNumber: number },
            }),
        onError: (error) => setSendError(toSendError(error)),
    });

    if (!hash) {
        return <Navigate to="/expired" replace />;
    }

    const isSubmitting = isSending || isSuccess;
    const invalid = touched && !isValidPhoneNumber(phoneNumber);

    // The user already finalized a recovery method, so a phone number can't be added
    if (sendError === 'finalized') {
        return (
            <PhoneFinalizedView
                onNext={() => navigate(`/recovery-code${query}`)}
            />
        );
    }

    return (
        <PhoneVerificationView
            phoneNumber={phoneNumber}
            onPhoneNumberChange={setPhoneNumber}
            onBack={() => navigate(`/recovery${query}`)}
            isSubmitting={isSubmitting}
            invalid={invalid}
            sendError={sendError}
            onSubmit={(e) => {
                e.preventDefault();
                // Enter submits even when the button is disabled
                if (isSubmitting || sendError === 'rateLimited') {
                    return;
                }
                setTouched(true);
                setSendError(null);
                if (isValidPhoneNumber(phoneNumber)) {
                    send(normalizePhoneNumber(phoneNumber));
                }
            }}
        />
    );
}
