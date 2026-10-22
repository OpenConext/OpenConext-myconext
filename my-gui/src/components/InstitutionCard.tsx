import { useState } from 'react';

import { BuildingOfficeIcon, CaretDownIcon } from '@phosphor-icons/react';
import { Badge, Button } from '@surfnet/curve-react';
import { useQuery } from '@tanstack/react-query';

import { fetchConfig } from '../api';
import type { LinkedAccount } from '../api';
import I18n from '../locale/I18n';
import { formatDate } from '../utils/format';
import {
    accountExpiresAt,
    accountFamilyName,
    accountGivenName,
    canDelete,
    institutionName,
    isExpired,
    roleNames,
} from '../utils/verification';
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
            className={`institution-card${bordered ? ' institution-card--bordered' : ''}`}
        >
            <button
                type="button"
                className="institution-card__header"
                aria-expanded={open}
                onClick={() => setOpen(!open)}
            >
                {account.logoUrl ? (
                    <img
                        className="institution-card__logo"
                        src={account.logoUrl}
                        alt=""
                    />
                ) : (
                    <span className="institution-card__logo institution-card__logo--placeholder">
                        <BuildingOfficeIcon />
                    </span>
                )}
                <span className="institution-card__title">
                    <span className="institution-card__name">
                        {institutionName(account)}
                        {expired && (
                            <Badge variant="secondary">
                                {I18n.t('institutions.expired')}
                            </Badge>
                        )}
                    </span>
                    {roles && (
                        <span className="institution-card__role">
                            {I18n.t('institutions.role', { roles })}
                        </span>
                    )}
                </span>
                <CaretDownIcon
                    className="institution-card__caret"
                    data-open={open ? '' : undefined}
                />
            </button>
            {open && (
                <>
                    <dl className="institution-card__details">
                        <dt>{I18n.t('institutions.details.firstName')}</dt>
                        <dd>{accountGivenName(account)}</dd>
                        <dt>{I18n.t('institutions.details.lastName')}</dt>
                        <dd>{accountFamilyName(account)}</dd>
                        <dt>{I18n.t('institutions.details.receivedOn')}</dt>
                        <dd>{formatDate(account.createdAt)}</dd>
                        <dt>{I18n.t('institutions.details.validUntil')}</dt>
                        <dd>{formatDate(accountExpiresAt(account, days))}</dd>
                    </dl>
                    {onDelete && canDelete(account) && (
                        <Button
                            variant="ghost"
                            className="institution-card__delete"
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
