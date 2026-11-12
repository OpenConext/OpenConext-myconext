import { NavLink, useNavigate } from 'react-router-dom';

import { CaretDownIcon } from '@phosphor-icons/react';
import {
    Button,
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
    NavigationMenu,
    NavigationMenuItem,
    NavigationMenuLink,
    NavigationMenuList,
} from '@surfnet/curve-react';

import { logout } from '../api';
import eduidLogo from '../icons/eduid-logo.svg?url';
import I18n from '../locale/I18n';
import { useAppStore } from '../stores/AppStore';
import './TopNavigation.scss';

const navigationItems = [
    { to: '/overview', labelKey: 'navigation.overview' },
    { to: '/users', labelKey: 'navigation.users' },
    { to: '/identity-check', labelKey: 'navigation.identityCheck' },
];

export const TopNavigation = () => {
    const user = useAppStore((state) => state.user);
    const navigate = useNavigate();
    const isMember = Boolean(user?.serviceDeskMember);

    const logoutUser = async () => {
        await logout();
        useAppStore.setState(() => ({ user: {}, controlCode: {} }));
        window.location.href = '/';
    };

    return (
        <header className="top-navigation">
            <div className="top-navigation__content">
                <NavLink
                    to="/"
                    className="top-navigation__logo"
                    onClick={(e) => {
                        e.preventDefault();
                        void navigate('/');
                    }}
                >
                    <img src={eduidLogo} alt="eduID" width={71} height={30} />
                </NavLink>
                {isMember && (
                    <NavigationMenu>
                        <NavigationMenuList>
                            {navigationItems.map(({ to, labelKey }) => (
                                <NavigationMenuItem key={to}>
                                    <NavLink to={to}>
                                        {({ isActive }) => (
                                            <NavigationMenuLink
                                                className="top-navigation__link"
                                                active={isActive}
                                                render={<span />}
                                            >
                                                {I18n.t(labelKey)}
                                            </NavigationMenuLink>
                                        )}
                                    </NavLink>
                                </NavigationMenuItem>
                            ))}
                        </NavigationMenuList>
                    </NavigationMenu>
                )}
                {isMember && (
                    <div className="top-navigation__actions">
                        <DropdownMenu>
                            <DropdownMenuTrigger
                                data-testid="user-menu"
                                render={<Button variant="ghost" />}
                            >
                                <span className="top-navigation__user">
                                    <span className="top-navigation__user-name">
                                        {user.displayName ?? user.name}
                                    </span>
                                    <span className="top-navigation__user-organisation">
                                        {user.schacHomeOrganization}
                                    </span>
                                </span>
                                <CaretDownIcon />
                            </DropdownMenuTrigger>
                            <DropdownMenuContent
                                align="end"
                                className="top-navigation__menu"
                            >
                                <DropdownMenuItem
                                    onClick={() => void logoutUser()}
                                >
                                    {I18n.t('header.links.logout')}
                                </DropdownMenuItem>
                            </DropdownMenuContent>
                        </DropdownMenu>
                    </div>
                )}
            </div>
        </header>
    );
};
