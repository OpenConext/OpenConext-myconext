import { useState } from 'react';
import { useSearchParams } from 'react-router';

import { UserListIcon, WarningCircleIcon } from '@phosphor-icons/react';
import {
    Alert,
    AlertAction,
    AlertDescription,
    AlertTitle,
    Button,
} from '@surfnet/curve-react';

import type { LinkedAccount } from '../../api';
import { DeleteInstitutionDialog } from '../../components/DeleteInstitutionDialog';
import { EditChosenNameDialog } from '../../components/EditChosenNameDialog';
import { EditNameDialog } from '../../components/EditNameDialog';
import { IdentityRow } from '../../components/IdentityRow';
import { NewLinkedAccount } from '../../components/NewLinkedAccount';
import { PageHeader } from '../../components/PageHeader';
import { VerifiedDataDialog } from '../../components/VerifiedDataDialog';
import { VerifyIdentityDialog } from '../../components/VerifyIdentityDialog';
import I18n from '../../locale/I18n';
import { useConfigStore } from '../../store/config';
import { useUserStore } from '../../store/me';
import { formatDate, formatDateOfBirth } from '../../utils/format';
import {
    accountKey,
    institutionName,
    isValid,
    isVerified,
    preferredAccount,
} from '../../utils/verification';
import './route.scss';

export default function Identity() {
    const me = useUserStore((state) => state.user);
    const config = useConfigStore((state) => state.config);
    const [editingName, setEditingName] = useState(false);
    const [editingChosenName, setEditingChosenName] = useState(false);
    const [verifyingState, setVerifying] = useState(false);
    const [viewing, setViewing] = useState<LinkedAccount>();
    const [deleting, setDeleting] = useState<LinkedAccount>();
    const [searchParams, setSearchParams] = useSearchParams();

    if (!me) {
        return null;
    }

    // the server and the error pages send the user back to continue verifying
    const retry = searchParams.has('retry');
    const serviceDesk = searchParams.has('servicedesk');
    const verifying = verifyingState || retry || serviceDesk;
    const closeVerify = () => {
        setVerifying(false);
        if (searchParams.size > 0) {
            setSearchParams({}, { replace: true });
        }
    };

    const controlCode = me.controlCode;
    const verified = isVerified(me);
    const account = preferredAccount(me);
    // only iDIN and eIDAS accounts deliver a date of birth
    const dobAccount = (me.externalLinkedAccounts ?? []).find(isValid);
    const accounts = [
        ...(me.linkedAccounts ?? []),
        ...(me.externalLinkedAccounts ?? []),
    ].sort((a, b) => b.createdAt - a.createdAt);
    const verifiedBy = account && institutionName(account);
    const verifiedText =
        account &&
        I18n.t('identity.verifiedBy', {
            name: verifiedBy,
            date: formatDate(account.createdAt),
        });

    return (
        <div className="identity">
            <PageHeader
                icon={<UserListIcon />}
                title={I18n.t('identity.title')}
                lead={I18n.t('identity.lead')}
            />

            {!verified && config?.enableAccountLinking !== false && (
                <Alert variant="info" className="identity__alert">
                    <WarningCircleIcon />
                    <AlertTitle>
                        {controlCode
                            ? I18n.t('identity.serviceDesk.bannerTitle')
                            : I18n.t('identity.unverified.title')}
                    </AlertTitle>
                    <AlertDescription>
                        {controlCode
                            ? I18n.t('identity.serviceDesk.banner')
                            : I18n.t('identity.unverified.description')}
                    </AlertDescription>
                    <AlertAction className="identity__alert-action">
                        <Button
                            variant="outline"
                            size="sm"
                            onClick={() => setVerifying(true)}
                        >
                            {controlCode
                                ? I18n.t('identity.serviceDesk.showCode')
                                : I18n.t('identity.unverified.button')}
                        </Button>
                    </AlertAction>
                </Alert>
            )}

            <div className="identity__list">
                <IdentityRow
                    testId="identity-chosen-name"
                    label={I18n.t('identity.chosenName')}
                    value={me.chosenName}
                    onEdit={() => setEditingChosenName(true)}
                />
                <IdentityRow
                    label={I18n.t('identity.name.label')}
                    value={`${me.givenName} ${me.familyName}`}
                    verifiedText={verifiedText}
                    onEdit={account ? undefined : () => setEditingName(true)}
                    onView={account && (() => setViewing(account))}
                />
                {dobAccount && me.dateOfBirth && (
                    <IdentityRow
                        label={I18n.t('identity.dateOfBirth')}
                        value={formatDateOfBirth(me.dateOfBirth)}
                        verifiedText={I18n.t('identity.verifiedBy', {
                            name: institutionName(dobAccount),
                            date: formatDate(dobAccount.createdAt),
                        })}
                        onView={() => setViewing(dobAccount)}
                    />
                )}
            </div>

            <EditNameDialog
                user={me}
                open={editingName}
                onOpenChange={setEditingName}
            />
            {editingChosenName && (
                <EditChosenNameDialog
                    user={me}
                    onClose={() => setEditingChosenName(false)}
                />
            )}
            {verifying && (
                <VerifyIdentityDialog
                    user={me}
                    initialView={
                        controlCode || serviceDesk
                            ? 'serviceDesk'
                            : retry && config?.featureIdVerify
                              ? 'banks'
                              : 'methods'
                    }
                    open
                    onOpenChange={closeVerify}
                />
            )}
            {viewing && (
                <VerifiedDataDialog
                    accounts={accounts}
                    openKey={accountKey(viewing)}
                    onDelete={(toDelete) => {
                        setViewing(undefined);
                        setDeleting(toDelete);
                    }}
                    onClose={() => setViewing(undefined)}
                />
            )}
            {deleting && (
                <DeleteInstitutionDialog
                    account={deleting}
                    onClose={() => setDeleting(undefined)}
                />
            )}
            <NewLinkedAccount />
        </div>
    );
}
