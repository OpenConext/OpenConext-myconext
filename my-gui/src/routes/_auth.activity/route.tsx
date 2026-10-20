import { useQuery } from '@tanstack/react-query';

import { fetchActivities } from '../../api';
import { ActivityList } from '../../components/ActivityList';
import I18n from '../../locale/I18n';

export default function Activity() {
    const { data: activities = [] } = useQuery({
        queryKey: ['activities'],
        queryFn: fetchActivities,
    });

    return (
        <>
            <h1>{I18n.t('pages.activity')}</h1>
            <h2>{I18n.t('activity.all')}</h2>
            <ActivityList activities={activities} />
        </>
    );
}
