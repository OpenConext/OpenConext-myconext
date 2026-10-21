import { useState } from 'react';

import { ClockIcon } from '@phosphor-icons/react';
import { useQuery } from '@tanstack/react-query';

import { fetchActivities, fetchConfig, fetchMe, fetchTokens } from '../../api';
import { ActivityList } from '../../components/ActivityList';
import { DeleteServiceDialog } from '../../components/DeleteServiceDialog';
import { PageHeader } from '../../components/PageHeader';
import { ServiceCard } from '../../components/ServiceCard';
import I18n from '../../locale/I18n';
import { type Service, buildServices } from '../../utils/services';
import './route.scss';

const PAGE_SIZE = 4;

export default function Activity() {
    // reference for the relative login times, fixed while on the page
    const [now] = useState(() => Date.now());
    const [action, setAction] = useState<{
        service: Service;
        kind: 'delete' | 'revoke';
    }>();
    const { data: me } = useQuery({ queryKey: ['me'], queryFn: fetchMe });
    const { data: config } = useQuery({
        queryKey: ['config'],
        queryFn: fetchConfig,
    });
    const { data: activities = [] } = useQuery({
        queryKey: ['activities'],
        queryFn: fetchActivities,
    });
    const { data: tokens = [] } = useQuery({
        queryKey: ['tokens'],
        queryFn: fetchTokens,
        enabled: Boolean(config?.featureOidcTokenAPI),
    });

    if (!me) {
        return null;
    }

    const services = buildServices(me, tokens);

    return (
        <div className="activity">
            <PageHeader
                color="turquoise"
                icon={<ClockIcon />}
                title={I18n.t('activity.title')}
                lead={I18n.t('activity.lead')}
            />

            <section className="activity__section">
                <h2 className="activity__heading">
                    {I18n.t('activity.services.title')}
                </h2>
                {services.length > 0 ? (
                    <div className="activity__card">
                        {services.map((service) => (
                            <ServiceCard
                                key={`${service.entityId}-${service.name}`}
                                service={service}
                                now={now}
                                onDelete={() =>
                                    setAction({ service, kind: 'delete' })
                                }
                                onRevoke={() =>
                                    setAction({ service, kind: 'revoke' })
                                }
                            />
                        ))}
                    </div>
                ) : (
                    <p className="activity__empty">
                        {I18n.t('activity.services.empty')}
                    </p>
                )}
            </section>

            {activities.length > 0 && (
                <section className="activity__section">
                    <h2 className="activity__heading">
                        {I18n.t('activity.all')}
                    </h2>
                    <ActivityList
                        activities={activities}
                        pageSize={PAGE_SIZE}
                    />
                </section>
            )}

            {action && (
                <DeleteServiceDialog
                    service={action.service}
                    kind={action.kind}
                    onClose={() => setAction(undefined)}
                />
            )}
        </div>
    );
}
