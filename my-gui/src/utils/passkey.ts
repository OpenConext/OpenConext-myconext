type SignalApi = {
    signalUnknownCredential?: (options: {
        rpId: string;
        credentialId: string;
    }) => Promise<void>;
};

// Tells the browser or password manager the passkey no longer exists, so it
// can remove it. Only a hint: returns false when the browser has no support or
// the signal fails, so the user can be told to remove it manually.
export const signalPasskeyDeleted = async (
    credentialId: string,
): Promise<boolean> => {
    const api = (globalThis as { PublicKeyCredential?: SignalApi })
        .PublicKeyCredential;
    if (typeof api?.signalUnknownCredential !== 'function') {
        return false;
    }
    try {
        await api.signalUnknownCredential({
            rpId: window.location.hostname,
            credentialId,
        });
        return true;
    } catch {
        return false;
    }
};
