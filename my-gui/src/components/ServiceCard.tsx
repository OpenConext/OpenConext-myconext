import { useState } from 'react';

import { AppWindowIcon } from '@phosphor-icons/react';
import { Button, Separator } from '@surfnet/curve-react';

import I18n from '../locale/I18n';
import { formatDate, formatRelative } from '../utils/format';
import type { Service } from '../utils/services';
import { ExpandableCardHeader } from './ExpandableCardHeader';
import './ServiceCard.scss';

type ServiceCardProps = {
    service: Service;
    // reference time for the relative login date
    now: number;
    onDelete: () => void;
    onRevoke: () => void;
};

export const ServiceCard = ({
    service,
    now,
    onDelete,
    onRevoke,
}: ServiceCardProps) => {
    const [open, setOpen] = useState(false);
    const { token } = service;

    return (
        <div className="service-card">
            <ExpandableCardHeader
                logoUrl={service.logoUrl}
                placeholderIcon={<AppWindowIcon />}
                name={service.name}
                subtitle={
                    service.lastLogin &&
                    I18n.t('activity.services.loggedIn', {
                        time: formatRelative(service.lastLogin, now),
                    })
                }
                open={open}
                onToggle={() => setOpen(!open)}
            />
            {open && (
                <div className="service-card__details">
                    {token && (
                        <>
                            <div className="service-card__section">
                                {service.scopeDescriptions.map((text) => (
                                    <p key={text}>{text}</p>
                                ))}
                                <dl className="service-card__list">
                                    <dt>
                                        {I18n.t(
                                            'activity.services.accessGiven',
                                        )}
                                    </dt>
                                    <dd>{formatDate(token.createdAt)}</dd>
                                    <dt>
                                        {I18n.t(
                                            'activity.services.accessExpires',
                                        )}
                                    </dt>
                                    <dd>{formatDate(token.expiresIn)}</dd>
                                </dl>
                            </div>
                            <Separator />
                        </>
                    )}
                    <div className="service-card__section">
                        <dl className="service-card__eduid">
                            <dt>{I18n.t('activity.services.uniqueEduId')}</dt>
                            <dd>{service.eduId}</dd>
                        </dl>
                        <dl className="service-card__list">
                            <dt>{I18n.t('activity.services.firstLogin')}</dt>
                            <dd>{formatDate(service.createdAt)}</dd>
                            {service.lastLogin && (
                                <>
                                    <dt>
                                        {I18n.t('activity.services.lastLogin')}
                                    </dt>
                                    <dd>{formatDate(service.lastLogin)}</dd>
                                </>
                            )}
                            {service.homeUrl && (
                                <>
                                    <dt>
                                        {I18n.t('activity.services.homePage')}
                                    </dt>
                                    <dd>
                                        <a
                                            href={service.homeUrl}
                                            target="_blank"
                                            rel="noreferrer"
                                        >
                                            {service.homeUrl}
                                        </a>
                                    </dd>
                                </>
                            )}
                        </dl>
                    </div>
                    <Separator />
                    <div className="service-card__actions">
                        <Button
                            variant="ghost"
                            className="service-card__action"
                            onClick={onDelete}
                        >
                            {I18n.t('activity.services.deleteLogin')}
                        </Button>
                        {token && (
                            <Button
                                variant="ghost"
                                className="service-card__action"
                                onClick={onRevoke}
                            >
                                {I18n.t('activity.services.revoke')}
                            </Button>
                        )}
                    </div>
                </div>
            )}
        </div>
    );
};
