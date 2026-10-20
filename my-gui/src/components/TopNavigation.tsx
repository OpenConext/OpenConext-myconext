import { NavLink, useLocation, useNavigate } from 'react-router';

import { CaretDownIcon } from '@phosphor-icons/react';
import {
    Button,
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
    NavigationMenu,
    NavigationMenuItem,
    NavigationMenuLink,
    NavigationMenuList,
} from '@surfnet/curve-react';
import { useMutation, useQuery } from '@tanstack/react-query';

import { fetchConfig, logout, updateLanguage } from '../api';
import eduidLogo from '../assets/eduid-logo.svg';
import { useBadgeCounts } from '../hooks/useBadgeCounts';
import I18n, { AVAILABLE_LANGUAGES, switchLocale } from '../locale/I18n';
import { navigationItems } from '../navigation';
import { useAppStore } from '../store/store';
import { CountBadge } from './CountBadge';
import './TopNavigation.scss';

type TopNavigationProps = {
    userName: string;
};

const isActive = (to: string, pathname: string) =>
    to === '/' ? pathname === '/' : pathname.startsWith(to);

export const TopNavigation = ({ userName }: TopNavigationProps) => {
    const { pathname } = useLocation();
    const navigate = useNavigate();
    const dark = useAppStore((state) => state.dark);
    const toggleDark = useAppStore((state) => state.toggleDark);

    const { mutate, isPending } = useMutation({
        mutationFn: logout,
        onSuccess: () => navigate('/landing?logout=true'),
    });

    const { data: config } = useQuery({
        queryKey: ['config'],
        queryFn: fetchConfig,
    });
    const badgeCounts = useBadgeCounts();
    const baseDomain =
        typeof config?.baseDomain === 'string' ? config.baseDomain : undefined;

    return (
        <header className="top-navigation">
            <div className="top-navigation__content">
                <NavLink to="/" className="top-navigation__logo">
                    <img
                        src={eduidLogo}
                        alt={I18n.t('logo.eduid')}
                        width={71}
                        height={30}
                    />
                </NavLink>
                <NavigationMenu>
                    <NavigationMenuList>
                        {navigationItems.map(({ to, labelKey, badge }) => (
                            <NavigationMenuItem key={to}>
                                <NavigationMenuLink
                                    className="top-navigation__link"
                                    active={isActive(to, pathname)}
                                    render={
                                        <NavLink to={to} end={to === '/'} />
                                    }
                                >
                                    {I18n.t(labelKey)}
                                    {badge && badgeCounts[badge] > 0 && (
                                        <CountBadge
                                            count={badgeCounts[badge]}
                                        />
                                    )}
                                </NavigationMenuLink>
                            </NavigationMenuItem>
                        ))}
                    </NavigationMenuList>
                </NavigationMenu>
                <div className="top-navigation__actions">
                    <DropdownMenu>
                        <DropdownMenuTrigger
                            render={<Button variant="ghost" />}
                        >
                            {I18n.t('code')}
                            <CaretDownIcon />
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end">
                            {AVAILABLE_LANGUAGES.map((lang) => (
                                <DropdownMenuItem
                                    key={lang}
                                    onClick={() =>
                                        switchLocale(
                                            lang,
                                            baseDomain,
                                            updateLanguage,
                                        )
                                    }
                                >
                                    {I18n.t(`languages.${lang}`)}
                                </DropdownMenuItem>
                            ))}
                        </DropdownMenuContent>
                    </DropdownMenu>
                    <DropdownMenu>
                        <DropdownMenuTrigger
                            render={<Button variant="ghost" />}
                        >
                            {userName}
                            <CaretDownIcon />
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end">
                            <DropdownMenuItem
                                onClick={() => navigate('/account')}
                            >
                                {I18n.t('userMenu.account')}
                            </DropdownMenuItem>
                            <DropdownMenuItem onClick={toggleDark}>
                                {dark
                                    ? I18n.t('userMenu.lightMode')
                                    : I18n.t('userMenu.darkMode')}
                            </DropdownMenuItem>
                            <DropdownMenuSeparator />
                            <DropdownMenuItem
                                disabled={isPending}
                                onClick={() => mutate()}
                            >
                                {I18n.t('userMenu.logout')}
                            </DropdownMenuItem>
                        </DropdownMenuContent>
                    </DropdownMenu>
                </div>
            </div>
        </header>
    );
};
