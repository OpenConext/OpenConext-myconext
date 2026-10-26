import { useState } from 'react';

import { BuildingOfficeIcon } from '@phosphor-icons/react';
import { Badge, Button } from '@surfnet/curve-react';
import { useQuery } from '@tanstack/react-query';

import { fetchConfig } from '../api';
import type { LinkedAccount } from '../api';
import I18n from '../locale/I18n';
import { formatDate } from '../utils/format';
import { timeAgo } from '../utils/timeAgo';
import {
    accountExpiresAt,
    accountFamilyName,
    accountGivenName,
    canDelete,
    institutionName,
    isExpired,
    roleNames,
} from '../utils/verification';
import { ExpandableCardHeader } from './ExpandableCardHeader';
import './InstitutionCard.scss';

type InstitutionCardProps = {
    account: LinkedAccount;
    // without it the details are read-only
    onDelete?: () => void;
    // starts with the details shown
    defaultOpen?: boolean;
    // a standalone card, for example in a dialog, instead of a list row
    bordered?: boolean;
};

export const InstitutionCard = ({
    account,
    onDelete,
    defaultOpen,
    bordered,
}: InstitutionCardProps) => {
    const [open, setOpen] = useState(defaultOpen ?? false);
    const { data: config } = useQuery({
        queryKey: ['config'],
        queryFn: fetchConfig,
    });
    const days = config?.expirationNonValidatedDurationDays;
    const expired = isExpired(account, days);
    const roles = roleNames(account);

    return (
        <div
            data-testid="institution-card"
            className={`institution-card${bordered ? ' institution-card--bordered' : ''}`}
        >
            <ExpandableCardHeader
                testId="institution-card-toggle"
                logoUrl={account.logoUrl}
                placeholderIcon={<BuildingOfficeIcon />}
                name={
                    <>
                        {institutionName(account)}
                        {expired && (
                            <Badge variant="secondary">
                                {I18n.t('institutions.expired')}
                            </Badge>
                        )}
                    </>
                }
                subtitle={I18n.t('institutions.added', {
                    time: timeAgo(account.createdAt),
                })}
                open={open}
                onToggle={() => setOpen(!open)}
            />
            {open && (
                <>
                    <dl className="institution-card__details">
                        <dt>{I18n.t('institutions.details.firstName')}</dt>
                        <dd>{accountGivenName(account)}</dd>
                        <dt>{I18n.t('institutions.details.lastName')}</dt>
                        <dd>{accountFamilyName(account)}</dd>
                        {roles && (
                            <>
                                <dt>{I18n.t('institutions.details.role')}</dt>
                                <dd>{roles}</dd>
                            </>
                        )}
                        <dt>{I18n.t('institutions.details.receivedOn')}</dt>
                        <dd>{formatDate(account.createdAt)}</dd>
                        <dt>{I18n.t('institutions.details.validUntil')}</dt>
                        <dd>{formatDate(accountExpiresAt(account, days))}</dd>
                    </dl>
                    {onDelete && canDelete(account) && (
                        <Button
                            variant="ghost"
                            className="institution-card__delete"
                            data-testid="institution-card-delete"
                            onClick={onDelete}
                        >
                            {I18n.t('institutions.delete.button')}
                        </Button>
                    )}
                </>
            )}
        </div>
    );
};
