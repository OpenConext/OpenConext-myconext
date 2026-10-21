import { useQuery } from '@tanstack/react-query';

import { fetchMe } from '../api';
import { isVerified } from '../utils/verification';

export type BadgeCounts = {
    identity: number;
    institutions: number;
    security: number;
};

export const useBadgeCounts = (): BadgeCounts => {
    const { data: me } = useQuery({ queryKey: ['me'], queryFn: fetchMe });

    return {
        identity: me && !isVerified(me) ? 1 : 0,
        institutions:
            (me?.linkedAccounts ?? []).length +
            (me?.externalLinkedAccounts ?? []).length,
        security: (me?.loginOptions ?? []).length,
    };
};
