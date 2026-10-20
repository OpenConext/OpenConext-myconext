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
        // TODO: derive from verification state once the identity page is built
        identity: (me?.externalLinkedAccounts ?? []).length === 0 ? 1 : 0,
        institutions:
            (me?.linkedAccounts ?? []).length +
            (me?.externalLinkedAccounts ?? []).length,
        security: (me?.loginOptions ?? []).length,
    };
};
