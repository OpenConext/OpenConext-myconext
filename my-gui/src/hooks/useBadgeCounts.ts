import { useUserStore } from '../store/me';

export type BadgeCounts = {
    identity: number;
    institutions: number;
    security: number;
};

export const useBadgeCounts = (): BadgeCounts => {
    const me = useUserStore((state) => state.user);

    return {
        // the identity tab always shows a badge, as in the design
        identity: 1,
        institutions:
            (me?.linkedAccounts ?? []).length +
            (me?.externalLinkedAccounts ?? []).length,
        security: (me?.loginOptions ?? []).length,
    };
};
