import { useSearchParams } from 'react-router';

import { useUserStore } from '../store/me';
import { isValid } from '../utils/verification';
import { LinkedAccountDialog } from './LinkedAccountDialog';

// the server sends the user back with `?institution=<id>` after linking an
// institution and with `?verify=<idp>` after linking iDIN or eIDAS
export const NewLinkedAccount = () => {
    const [searchParams, setSearchParams] = useSearchParams();
    const me = useUserStore((state) => state.user);

    const institution = searchParams.get('institution');
    const verify = searchParams.get('verify');
    if (!me || (!institution && !verify)) {
        return null;
    }

    const external = me.externalLinkedAccounts ?? [];
    const institutions = me.linkedAccounts ?? [];
    const account = verify
        ? external[0]
        : institutions.find(
              (a) =>
                  a.eduPersonPrincipalName === institution ||
                  a.subjectId === institution,
          );
    // accounts without names do not deliver anything to confirm
    const hasNames =
        account?.external || (account?.givenName && account.familyName);
    if (!account || !hasNames) {
        return null;
    }

    const accounts = [...institutions, ...external].filter(
        (a) => isValid(a) && (a.external || (a.givenName && a.familyName)),
    );

    return (
        <LinkedAccountDialog
            account={account}
            accounts={accounts}
            onClose={() => setSearchParams({}, { replace: true })}
        />
    );
};
