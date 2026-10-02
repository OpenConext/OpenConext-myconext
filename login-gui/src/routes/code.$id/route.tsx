import { useEffect, useRef, useState } from 'react';
import { useLocation, useParams } from 'react-router';

import { Button, Input } from '@surfnet/curve-react';
import { useMutation } from '@tanstack/react-query';

import { generateCodeRequest, verifyCodeRequest } from '../../api';

export default function Code() {
    const { id = '' } = useParams();
    const { state } = useLocation();
    const email: string = state?.email ?? '';

    const [code, setCode] = useState('');
    const hasFired = useRef(false);

    const {
        mutate: generateCode,
        isPending: isGenerating,
        isError: generateFailed,
    } = useMutation({
        mutationFn: () => generateCodeRequest(email, id),
    });

    useEffect(() => {
        if (hasFired.current) {
            return;
        }

        hasFired.current = true;
        generateCode();
    }, []);

    const {
        mutate: submitCode,
        isPending: isVerifying,
        isError: verifyFailed,
    } = useMutation({
        mutationFn: (code: string) => verifyCodeRequest(code, id),
        onSuccess: ({ url }) => (window.location.href = url),
    });

    if (isGenerating) {
        return <div>Sending code...</div>;
    }

    if (generateFailed) {
        return <div>Failed to send code. Please try again.</div>;
    }

    return (
        <div>
            <p>A code has been sent to {email}. Enter it below to continue.</p>
            <Input
                type="text"
                value={code}
                onChange={(e) => setCode(e.target.value)}
            />
            {verifyFailed && <p>Invalid code, please try again.</p>}

            <Button onClick={() => submitCode(code)} disabled={isVerifying}>
                Continue
            </Button>
        </div>
    );
}
