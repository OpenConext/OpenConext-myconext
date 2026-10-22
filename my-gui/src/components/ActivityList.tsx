import { useState } from 'react';

import { PlusCircleIcon } from '@phosphor-icons/react';
import {
    Button,
    Card,
    Item,
    ItemActions,
    ItemContent,
    ItemGroup,
    ItemTitle,
} from '@surfnet/curve-react';

import type { Activity } from '../api';
import I18n from '../locale/I18n';
import { activityText } from '../utils/activity';
import { formatDateTime } from '../utils/format';
import './ActivityList.scss';

type ActivityListProps = {
    activities: Activity[];
    // show this many at a time with a button for more, all when omitted
    pageSize?: number;
};

export const ActivityList = ({ activities, pageSize }: ActivityListProps) => {
    const [visible, setVisible] = useState(pageSize ?? activities.length);
    const shown = pageSize ? activities.slice(0, visible) : activities;

    return (
        <Card className="activity-list">
            <ItemGroup className="activity-list__group">
                {shown.map((activity) => (
                    <Item
                        key={`${activity.event}-${activity.dateTime}`}
                        className="activity-list__row"
                    >
                        <ItemContent>
                            <ItemTitle className="activity-list__text">
                                {activityText(activity)}
                            </ItemTitle>
                        </ItemContent>
                        <ItemActions className="activity-list__date">
                            {formatDateTime(activity.dateTime)}
                        </ItemActions>
                    </Item>
                ))}
            </ItemGroup>
            {pageSize && visible < activities.length && (
                <Button
                    variant="ghost"
                    className="activity-list__more"
                    onClick={() => setVisible(visible + pageSize)}
                >
                    <PlusCircleIcon />
                    {I18n.t('activity.showMore')}
                </Button>
            )}
        </Card>
    );
};
