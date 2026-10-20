import { Badge } from '@surfnet/curve-react';

import './CountBadge.scss';

type CountBadgeProps = {
    count: number;
};

export const CountBadge = ({ count }: CountBadgeProps) => (
    <Badge variant="info" className="count-badge">
        {count}
    </Badge>
);
