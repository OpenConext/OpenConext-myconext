import { Link } from 'react-router';

import {
    ArrowRightIcon,
    WarningCircleIcon,
    WarningDiamondIcon,
} from '@phosphor-icons/react';
import {
    Alert,
    AlertDescription,
    AlertTitle,
    Button,
    buttonVariants,
} from '@surfnet/curve-react';

import activityBadge from '../../assets/home/activity-badge.svg';
import activity from '../../assets/home/activity.svg';
import identityBadge from '../../assets/home/identity-badge.svg';
import identity from '../../assets/home/identity.svg';
import institutionsBadge from '../../assets/home/institutions-badge.svg';
import institutions from '../../assets/home/institutions.svg';
import securityBadge from '../../assets/home/security-badge.svg';
import security from '../../assets/home/security.svg';
import { ActivityList } from '../../components/ActivityList';
import { HomeCard } from '../../components/HomeCard';
import { useBadgeCounts } from '../../hooks/useBadgeCounts';
import I18n from '../../locale/I18n';
import { useConfigStore } from '../../store/config';
import { useUserStore } from '../../store/me';
import { useAppStore } from '../../store/store';
import { activityText, withHistory } from '../../utils/activity';
import { formatDateTime } from '../../utils/format';
import './route.scss';

const RECENT_LIMIT = 4;

export default function Home() {
    const me = useUserStore((state) => state.user);
    const config = useConfigStore((state) => state.config);
    const linking = config?.enableAccountLinking !== false;
    const counts = useBadgeCounts();
    const recentActivities = me
        ? withHistory(me, me.recentActivities ?? [], RECENT_LIMIT)
        : [];
    const introHidden = useAppStore((state) => state.introHidden);
    const hideIntro = useAppStore((state) => state.hideIntro);

    return (
        <div className="home">
            <header className="home__header">
                <h1 className="home__title">
                    {I18n.t('home.title', { name: me?.chosenName })}
                </h1>
                <p className="home__lead">{I18n.t('home.lead')}</p>
            </header>

            {me?.controlCode && (
                <Alert variant="warning" className="home__alert">
                    <WarningDiamondIcon />
                    <AlertTitle>{I18n.t('home.serviceDesk.title')}</AlertTitle>
                    <AlertDescription>
                        {I18n.t('home.serviceDesk.description')}
                    </AlertDescription>
                    <div className="home__alert-action home__alert-actions">
                        <Button
                            size="sm"
                            nativeButton={false}
                            render={<Link to="/identity?servicedesk" />}
                        >
                            {I18n.t('home.serviceDesk.showCode')}
                        </Button>
                        <Button
                            variant="outline"
                            size="sm"
                            nativeButton={false}
                            render={
                                <a
                                    href={I18n.t(
                                        'identity.serviceDesk.locationsUrl',
                                    )}
                                    target="_blank"
                                    rel="noreferrer"
                                />
                            }
                        >
                            {I18n.t('home.serviceDesk.locations')}
                        </Button>
                    </div>
                </Alert>
            )}

            {!introHidden && (
                <Alert variant="info" className="home__alert">
                    <WarningCircleIcon />
                    <AlertTitle>{I18n.t('home.intro.title')}</AlertTitle>
                    <AlertDescription>
                        {I18n.t('home.intro.description')}
                    </AlertDescription>
                    <div className="home__alert-action">
                        <Button variant="outline" size="sm" onClick={hideIntro}>
                            {I18n.t('home.intro.dismiss')}
                        </Button>
                    </div>
                </Alert>
            )}

            <div className="home__cards">
                <HomeCard
                    to="/identity"
                    title={I18n.t('home.cards.identity.title')}
                    description={I18n.t(
                        counts.identity > 0
                            ? 'home.cards.identity.unverified'
                            : 'home.cards.identity.verified',
                    )}
                    color="green"
                    icon={identity}
                    badgeIcon={identityBadge}
                    count={counts.identity}
                />
                {linking && (
                    <HomeCard
                        to="/institutions"
                        title={I18n.t('home.cards.institutions.title')}
                        description={
                            counts.institutions > 0
                                ? I18n.t('home.cards.institutions.count', {
                                      count: counts.institutions,
                                  })
                                : I18n.t('home.cards.institutions.none')
                        }
                        color="purple"
                        icon={institutions}
                        badgeIcon={institutionsBadge}
                        count={counts.institutions}
                    />
                )}
                <HomeCard
                    to="/security"
                    title={I18n.t('home.cards.security.title')}
                    description={
                        counts.security > 0
                            ? I18n.t('home.cards.security.count', {
                                  count: counts.security,
                              })
                            : I18n.t('home.cards.security.none')
                    }
                    color="blue"
                    icon={security}
                    badgeIcon={securityBadge}
                    count={counts.security}
                />
                <HomeCard
                    to="/activity"
                    title={I18n.t('home.cards.activity.title')}
                    description={
                        recentActivities.length > 0
                            ? `${activityText(recentActivities[0])}, ${formatDateTime(recentActivities[0].dateTime)}`
                            : I18n.t('home.activity.empty')
                    }
                    color="turquoise"
                    icon={activity}
                    badgeIcon={activityBadge}
                />
            </div>

            {recentActivities.length > 0 && (
                <section className="home__activity">
                    <div className="home__activity-header">
                        <h2 className="home__activity-title">
                            {I18n.t('home.activity.title')}
                        </h2>
                        <Link
                            to="/activity"
                            className={buttonVariants({ variant: 'link' })}
                        >
                            {I18n.t('home.activity.viewAll')}
                            <ArrowRightIcon />
                        </Link>
                    </div>
                    <ActivityList activities={recentActivities} />
                </section>
            )}
        </div>
    );
}
