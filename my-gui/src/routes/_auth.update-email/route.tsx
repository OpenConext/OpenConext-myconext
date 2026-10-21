import { useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router';

import { toast } from 'sonner';

import { Alert, AlertDescription, Spinner } from '@surfnet/curve-react';
import { useQuery, useQueryClient } from '@tanstack/react-query';

import { confirmEmail } from '../../api';
import I18n from '../../locale/I18n';

// the change is applied by the hash from the mail or from the verified code
export default function UpdateEmail() {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const queryClient = useQueryClient();
    const hash = searchParams.get('h') ?? '';
    // from the security page, otherwise the user came from a mailed link
    const target =
        searchParams.get('nav') === 'security' ? '/security' : '/identity';

    const { data: user, isError } = useQuery({
        queryKey: ['confirmEmail', hash],
        queryFn: () => confirmEmail(hash),
        enabled: Boolean(hash),
        staleTime: Infinity,
        gcTime: 0,
        retry: false,
        refetchOnWindowFocus: false,
    });

    const failed = isError || !hash;

    useEffect(() => {
        if (user) {
            queryClient.setQueryData(['me'], user);
            toast.success(I18n.t('toast.emailConfirmed'));
            void navigate(target, { replace: true });
        }
    }, [user, queryClient, navigate, target]);

    if (failed) {
        return (
            <Alert variant="danger">
                <AlertDescription>
                    {I18n.t('security.emailFlow.confirmFailed')}
                </AlertDescription>
            </Alert>
        );
    }

    return <Spinner />;
}
