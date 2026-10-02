import { NavLink, useNavigate } from 'react-router';

import { Button } from '@surfnet/curve-react';
import { useMutation } from '@tanstack/react-query';

import { logout } from '../api';
import { useAppStore } from '../store/store';
import './NavigationBar.scss';

export const NavigationBar = () => {
    const dark = useAppStore((state) => state.dark);
    const toggleDark = useAppStore((state) => state.toggleDark);
    const navigate = useNavigate();

    const { mutate, isPending } = useMutation({
        mutationFn: logout,
        onSuccess: () => {
            navigate('/landing?logout=true');
        },
    });

    return (
        <nav className="navigation-bar">
            <NavLink to="/">[HOME]</NavLink>
            <NavLink to="/about">[ABOUT]</NavLink>
            <NavLink to="/security">[Security (Authenticated only)]</NavLink>
            <Button onClick={toggleDark}>
                {dark ? '☀️ Light mode' : '🌙 Dark mode'}
            </Button>
            <Button onClick={() => mutate()} disabled={isPending}>
                Logout
            </Button>
        </nav>
    );
};
