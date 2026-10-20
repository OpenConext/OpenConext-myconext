import {
    Card,
    Item,
    ItemActions,
    ItemContent,
    ItemGroup,
    ItemTitle,
} from '@surfnet/curve-react';

import type { Activity } from '../api';
import I18n from '../locale/I18n';
import { formatDateTime } from '../utils/format';
import './ActivityList.scss';

type ActivityListProps = {
    activities: Activity[];
};

export const ActivityList = ({ activities }: ActivityListProps) => (
    <Card className="activity-list">
        <ItemGroup className="activity-list__group">
            {activities.map(({ event, dateTime }) => (
                <Item
                    key={`${event}-${dateTime}`}
                    className="activity-list__row"
                >
                    <ItemContent>
                        <ItemTitle className="activity-list__text">
                            {I18n.t(`activity.events.${event}`)}
                        </ItemTitle>
                    </ItemContent>
                    <ItemActions className="activity-list__date">
                        {formatDateTime(dateTime)}
                    </ItemActions>
                </Item>
            ))}
        </ItemGroup>
    </Card>
);
