import { useEffect, useState } from 'react';

const COPIED_TIMEOUT = 1150;

// the server shows the recovery code in two parts
export const formatRecoveryCode = (code: string): string =>
    `${code.substring(0, 4)} ${code.substring(4)}`;

// `copied` resets itself, so it can be used to flash a confirmation label
export const useCopyToClipboard = (text: string) => {
    const [copied, setCopied] = useState(false);

    useEffect(() => {
        if (!copied) {
            return;
        }
        const timer = setTimeout(() => setCopied(false), COPIED_TIMEOUT);
        return () => clearTimeout(timer);
    }, [copied]);

    const copy = async () => {
        await navigator.clipboard.writeText(text);
        setCopied(true);
    };

    return { copied, copy };
};

// leaving the page would lose a recovery code that is only shown once
export const useWarnBeforeUnload = (active: boolean): void => {
    useEffect(() => {
        if (!active) {
            return;
        }
        const warn = (event: BeforeUnloadEvent) => event.preventDefault();
        window.addEventListener('beforeunload', warn);
        return () => window.removeEventListener('beforeunload', warn);
    }, [active]);
};
