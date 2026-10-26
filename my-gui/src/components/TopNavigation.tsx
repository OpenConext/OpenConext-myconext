import { useState } from 'react';
import { NavLink, useLocation, useNavigate } from 'react-router';

import {
    CaretDownIcon,
    CloudArrowDownIcon,
    ListIcon,
    MoonIcon,
    SignOutIcon,
    SunIcon,
    TrashIcon,
} from '@phosphor-icons/react';
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
    Sheet,
    SheetClose,
    SheetContent,
    SheetTitle,
    SheetTrigger,
} from '@surfnet/curve-react';
import { useMutation } from '@tanstack/react-query';

import { logout } from '../api';
import eduidLogo from '../assets/eduid-logo.svg';
import { useBadgeCounts } from '../hooks/useBadgeCounts';
import I18n from '../locale/I18n';
import { navigationItems } from '../navigation';
import { useConfigStore } from '../store/config';
import { useAppStore } from '../store/store';
import { CountBadge } from './CountBadge';
import { DeleteAccountDialog } from './DeleteAccountDialog';
import { DownloadDataDialog } from './DownloadDataDialog';
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
    const [deleting, setDeleting] = useState(false);
    const [downloading, setDownloading] = useState(false);

    const { mutate, isPending } = useMutation({
        mutationFn: logout,
        onSuccess: () => navigate('/landing?logout=true'),
    });

    const config = useConfigStore((state) => state.config);
    const badgeCounts = useBadgeCounts();
    // institutions are hidden when the server disabled linking accounts
    const visibleItems = navigationItems.filter(
        ({ to }) =>
            to !== '/institutions' || config?.enableAccountLinking !== false,
    );

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
                <NavigationMenu className="top-navigation__desktop">
                    <NavigationMenuList>
                        {visibleItems.map(({ to, labelKey, badge }) => (
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
                <div className="top-navigation__actions top-navigation__desktop">
                    <DropdownMenu>
                        <DropdownMenuTrigger
                            data-testid="user-menu"
                            render={<Button variant="ghost" />}
                        >
                            {userName}
                            <CaretDownIcon />
                        </DropdownMenuTrigger>
                        <DropdownMenuContent
                            align="end"
                            className="top-navigation__menu"
                        >
                            <DropdownMenuItem
                                onClick={() => setDownloading(true)}
                            >
                                <CloudArrowDownIcon />
                                {I18n.t('userMenu.downloadData')}
                            </DropdownMenuItem>
                            <DropdownMenuItem onClick={toggleDark}>
                                {dark ? <SunIcon /> : <MoonIcon />}
                                {dark
                                    ? I18n.t('userMenu.lightMode')
                                    : I18n.t('userMenu.darkMode')}
                            </DropdownMenuItem>
                            <DropdownMenuItem
                                disabled={isPending}
                                onClick={() => mutate()}
                            >
                                <SignOutIcon />
                                {I18n.t('userMenu.logout')}
                            </DropdownMenuItem>
                            <DropdownMenuSeparator />
                            <DropdownMenuItem
                                data-testid="user-menu-delete-account"
                                onClick={() => setDeleting(true)}
                            >
                                <TrashIcon />
                                {I18n.t('userMenu.deleteAccount')}
                            </DropdownMenuItem>
                        </DropdownMenuContent>
                    </DropdownMenu>
                </div>
                <Sheet>
                    <SheetTrigger
                        className="top-navigation__burger"
                        render={<Button variant="ghost" size="icon" />}
                        aria-label={I18n.t('navigation.menu')}
                    >
                        <ListIcon />
                    </SheetTrigger>
                    <SheetContent className="top-navigation__sheet">
                        <SheetTitle>{I18n.t('navigation.menu')}</SheetTitle>
                        <nav className="top-navigation__sheet-links">
                            {visibleItems.map(({ to, labelKey, badge }) => (
                                <SheetClose
                                    key={to}
                                    className="top-navigation__sheet-link"
                                    data-active={
                                        isActive(to, pathname) ? '' : undefined
                                    }
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
                                </SheetClose>
                            ))}
                        </nav>
                        <div className="top-navigation__sheet-section">
                            <Button
                                variant="ghost"
                                onClick={() => setDownloading(true)}
                            >
                                <CloudArrowDownIcon />
                                {I18n.t('userMenu.downloadData')}
                            </Button>
                            <Button variant="ghost" onClick={toggleDark}>
                                {dark ? <SunIcon /> : <MoonIcon />}
                                {dark
                                    ? I18n.t('userMenu.lightMode')
                                    : I18n.t('userMenu.darkMode')}
                            </Button>
                            <Button
                                variant="ghost"
                                disabled={isPending}
                                onClick={() => mutate()}
                            >
                                <SignOutIcon />
                                {I18n.t('userMenu.logout')}
                            </Button>
                            <Button
                                variant="ghost"
                                onClick={() => setDeleting(true)}
                            >
                                <TrashIcon />
                                {I18n.t('userMenu.deleteAccount')}
                            </Button>
                        </div>
                    </SheetContent>
                </Sheet>
            </div>
            {downloading && (
                <DownloadDataDialog onClose={() => setDownloading(false)} />
            )}
            {deleting && (
                <DeleteAccountDialog onClose={() => setDeleting(false)} />
            )}
        </header>
    );
};
