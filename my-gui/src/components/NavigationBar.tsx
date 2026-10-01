import { NavLink } from 'react-router';

import { Button } from '@surfnet/curve-react';

import { useAppStore } from '../store/store';
import './NavigationBar.scss';

export const NavigationBar = () => {
    const dark = useAppStore((state) => state.dark);
    const toggleDark = useAppStore((state) => state.toggleDark);

    return (
        <nav className="navigation-bar">
            <NavLink to="/">[HOME]</NavLink>
            <NavLink to="/about">[ABOUT]</NavLink>
            <NavLink to="/security">[Security (Authenticated only)]</NavLink>
            <Button onClick={toggleDark}>
                {dark ? '☀️ Light mode' : '🌙 Dark mode'}
            </Button>
        </nav>
    );
};
