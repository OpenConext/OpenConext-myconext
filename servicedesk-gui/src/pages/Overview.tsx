import { Link } from 'react-router-dom';

import {
    CaretRightIcon,
    ChartLineIcon,
    ShieldCheckIcon,
    UsersThreeIcon,
} from '@phosphor-icons/react';
import {
    Item,
    ItemActions,
    ItemContent,
    ItemMedia,
    ItemTitle,
} from '@surfnet/curve-react';

import { PageHeader } from '../components/PageHeader';
import I18n from '../locale/I18n';
import './Overview.scss';

const links = [
    { to: '/users', labelKey: 'overview.findUser', Icon: UsersThreeIcon },
    {
        to: '/identity-check',
        labelKey: 'overview.identityCheck',
        Icon: ShieldCheckIcon,
    },
    { to: '/stats', labelKey: 'overview.stats', Icon: ChartLineIcon },
];

export const Overview = () => (
    <main className="page-main">
        <PageHeader subtitle={I18n.t('overview.description')}>
            {I18n.t('overview.title')}
        </PageHeader>
        <nav className="overview">
            {links.map(({ to, labelKey, Icon }) => (
                <Item
                    key={to}
                    variant="outline"
                    className="overview__link"
                    render={<Link to={to} />}
                >
                    <ItemMedia className="overview__icon">
                        <Icon size={24} />
                    </ItemMedia>
                    <ItemContent>
                        <ItemTitle className="overview__title">
                            {I18n.t(labelKey)}
                        </ItemTitle>
                    </ItemContent>
                    <ItemActions className="overview__caret">
                        <CaretRightIcon size={18} />
                    </ItemActions>
                </Item>
            ))}
        </nav>
    </main>
);
