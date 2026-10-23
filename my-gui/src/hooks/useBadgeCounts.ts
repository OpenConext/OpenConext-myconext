import { useQuery } from '@tanstack/react-query';

import { fetchMe } from '../api';

export type BadgeCounts = {
    identity: number;
    institutions: number;
    security: number;
};

export const useBadgeCounts = (): BadgeCounts => {
    const { data: me } = useQuery({ queryKey: ['me'], queryFn: fetchMe });

    return {
        // the identity tab always shows a badge, as in the design
        identity: 1,
        institutions:
            (me?.linkedAccounts ?? []).length +
            (me?.externalLinkedAccounts ?? []).length,
        security: (me?.loginOptions ?? []).length,
    };
};
