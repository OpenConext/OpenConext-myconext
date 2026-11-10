import { useEffect } from 'react';

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
