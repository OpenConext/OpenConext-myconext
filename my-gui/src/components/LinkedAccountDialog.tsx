import { useState } from 'react';

import { toast } from 'sonner';

import { BuildingOfficeIcon } from '@phosphor-icons/react';
import {
    Badge,
    Button,
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
    RadioGroup,
    RadioGroupItem,
    Spinner,
} from '@surfnet/curve-react';
import { useMutation } from '@tanstack/react-query';

import { preferLinkedAccount } from '../api';
import type { LinkedAccount } from '../api';
import I18n from '../locale/I18n';
import { setUser } from '../store/me';
import { formatDateOfBirth } from '../utils/format';
import {
    accountFamilyName,
    accountGivenName,
    institutionName,
} from '../utils/verification';
import { InstitutionCard } from './InstitutionCard';
import './LinkedAccountDialog.scss';

type LinkedAccountDialogProps = {
    // the account that was just linked
    account: LinkedAccount;
    // all valid accounts including the new one
    accounts: LinkedAccount[];
    onClose: () => void;
};

const identifier = (account: LinkedAccount) =>
    `${account.external}-${account.subjectId ?? account.eduPersonPrincipalName ?? account.idpScoping}`;

// mounted only while open
export const LinkedAccountDialog = ({
    account,
    accounts,
    onClose,
}: LinkedAccountDialogProps) => {
    // with more accounts the user decides which one the names come from
    const choose = accounts.length > 1;
    const [selected, setSelected] = useState(identifier(account));

    const { mutate, isPending } = useMutation({
        mutationFn: preferLinkedAccount,
        onSuccess: (updated, chosen) => {
            setUser(updated);
            toast.success(
                I18n.t('toast.institutionPreferred', {
                    name: institutionName(chosen),
                }),
            );
            onClose();
        },
    });

    const handleContinue = () => {
        const chosen = accounts.find((a) => identifier(a) === selected);
        if (chosen && !chosen.preferred) {
            mutate(chosen);
        } else {
            onClose();
        }
    };

    return (
        <Dialog open onOpenChange={(open) => !open && onClose()}>
            <DialogContent className="linked-account" initialFocus={false}>
                <DialogHeader>
                    <DialogTitle className="linked-account__title">
                        {I18n.t('institutions.linked.title')}
                    </DialogTitle>
                    <DialogDescription>
                        {I18n.t(
                            choose
                                ? 'institutions.linked.choose'
                                : 'institutions.linked.intro',
                        )}
                    </DialogDescription>
                </DialogHeader>

                {choose ? (
                    <RadioGroup
                        className="linked-account__choices"
                        value={selected}
                        onValueChange={(value) => setSelected(value as string)}
                    >
                        {accounts.map((option) => (
                            <label
                                key={identifier(option)}
                                className="linked-account__choice"
                            >
                                <RadioGroupItem value={identifier(option)} />
                                <AccountLogo account={option} />
                                <span className="linked-account__text">
                                    <span className="linked-account__name">
                                        {institutionName(option)}
                                        <Badge
                                            variant={
                                                identifier(option) ===
                                                identifier(account)
                                                    ? 'default'
                                                    : 'secondary'
                                            }
                                        >
                                            {I18n.t(
                                                identifier(option) ===
                                                    identifier(account)
                                                    ? 'institutions.linked.new'
                                                    : 'institutions.linked.current',
                                            )}
                                        </Badge>
                                    </span>
                                    <span className="linked-account__detail">
                                        {accountGivenName(option)}{' '}
                                        {accountFamilyName(option)}
                                    </span>
                                </span>
                            </label>
                        ))}
                    </RadioGroup>
                ) : (
                    <>
                        <dl className="linked-account__details">
                            <dt>{I18n.t('institutions.details.firstName')}</dt>
                            <dd>{accountGivenName(account)}</dd>
                            <dt>{I18n.t('institutions.details.lastName')}</dt>
                            <dd>{accountFamilyName(account)}</dd>
                            {account.dateOfBirth && (
                                <>
                                    <dt>
                                        {I18n.t(
                                            'institutions.linked.dateOfBirth',
                                        )}
                                    </dt>
                                    <dd>
                                        {formatDateOfBirth(account.dateOfBirth)}
                                    </dd>
                                </>
                            )}
                        </dl>
                        <p className="linked-account__info">
                            {I18n.t('institutions.linked.alsoLinked')}
                        </p>
                        <InstitutionCard account={account} bordered />
                    </>
                )}

                <DialogFooter>
                    <Button
                        className="linked-account__continue"
                        disabled={isPending}
                        onClick={handleContinue}
                    >
                        {isPending && <Spinner />}
                        {I18n.t('institutions.linked.continue')}
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
};

const AccountLogo = ({ account }: { account: LinkedAccount }) =>
    account.logoUrl ? (
        <img className="linked-account__logo" src={account.logoUrl} alt="" />
    ) : (
        <span className="linked-account__logo linked-account__logo--placeholder">
            <BuildingOfficeIcon />
        </span>
    );
