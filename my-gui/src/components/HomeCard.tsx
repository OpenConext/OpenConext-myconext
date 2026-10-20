import { Link } from 'react-router';

import {
    Card,
    CardDescription,
    CardHeader,
    CardTitle,
} from '@surfnet/curve-react';

import { CountBadge } from './CountBadge';
import './HomeCard.scss';

export type HomeCardColor = 'green' | 'purple' | 'blue' | 'turquoise';

type HomeCardProps = {
    to: string;
    title: string;
    description: string;
    color: HomeCardColor;
    icon: string;
    badgeIcon: string;
    count?: number;
};

export const HomeCard = ({
    to,
    title,
    description,
    color,
    icon,
    badgeIcon,
    count,
}: HomeCardProps) => (
    <Link to={to} className="home-card__link">
        <Card className={`home-card home-card--${color}`}>
            <div className="home-card__media">
                <div className="home-card__icon">
                    <img src={icon} alt="" width={48} height={48} />
                    <span className="home-card__badge">
                        <img src={badgeIcon} alt="" width={16} height={16} />
                    </span>
                </div>
            </div>
            <CardHeader className="home-card__header">
                <CardTitle className="home-card__title">
                    {title}
                    {count ? <CountBadge count={count} /> : null}
                </CardTitle>
                <CardDescription className="home-card__description">
                    {description}
                </CardDescription>
            </CardHeader>
        </Card>
    </Link>
);
