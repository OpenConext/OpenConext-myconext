import { useEffect, useState } from 'react';

const COPIED_TIMEOUT = 1150;

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
