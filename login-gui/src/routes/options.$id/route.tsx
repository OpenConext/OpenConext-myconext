import type { ComponentType } from 'react';
import { Navigate, useLocation, useNavigate, useParams } from 'react-router';

import {
    DeviceMobileIcon,
    EnvelopeIcon,
    KeyIcon,
    PasswordIcon,
} from '@phosphor-icons/react';
import type { IconProps } from '@phosphor-icons/react';
import {
    Item,
    ItemContent,
    ItemGroup,
    ItemMedia,
    ItemTitle,
} from '@surfnet/curve-react';
import { useQuery } from '@tanstack/react-query';

import { fetchLoginMethods } from '../../api';
import { LoginContainer } from '../../components';
import I18n from '../../locale/I18n';
import styles from './route.module.scss';

type LoginOptionKey = 'useApp' | 'useWebAuthn' | 'useCode' | 'usePassword';

const OPTION_ICONS: Record<LoginOptionKey, ComponentType<IconProps>> = {
    useApp: DeviceMobileIcon,
    useWebAuthn: KeyIcon,
    useCode: EnvelopeIcon,
    usePassword: PasswordIcon,
};

const isLoginOptionKey = (key: string): key is LoginOptionKey =>
    key in OPTION_ICONS;

export default function Options() {
    const { id = '' } = useParams();
    const { state } = useLocation();
    const navigate = useNavigate();
    const email: string = state?.email ?? '';

    const {
        data: methods,
        isPending,
        isError,
    } = useQuery({
        queryKey: ['loginMethods', email],
        queryFn: () => fetchLoginMethods(email),
        enabled: email !== '',
    });

    if (!email) {
        return <Navigate to={`/login/${id}`} replace />;
    }

    const options = methods?.filter(isLoginOptionKey) ?? [];

    // App and security key flows have no page in this app yet.
    const selectOption = (option: LoginOptionKey) => {
        if (option === 'useCode') {
            navigate(`/code/${id}`, { state: { email } });
        } else if (option === 'usePassword') {
            navigate(`/usepassword/${id}`, { state: { email } });
        }
    };

    return (
        <LoginContainer
            title={I18n.t('options.title')}
            description={I18n.t('options.description')}
            onBack={() => navigate(`/login/${id}`)}
            backLabel={email}
        >
            {isPending && I18n.t('options.loading')}
            {isError && I18n.t('options.error')}
            {methods && (
                <ItemGroup>
                    {options.map((option) => {
                        const Icon = OPTION_ICONS[option];

                        return (
                            <Item
                                key={option}
                                variant="outline"
                                className={styles.item}
                                render={
                                    <button
                                        type="button"
                                        onClick={() => selectOption(option)}
                                    />
                                }
                            >
                                <ItemMedia>
                                    <Icon aria-hidden />
                                </ItemMedia>
                                <ItemContent>
                                    <ItemTitle>
                                        {I18n.t(`options.${option}`)}
                                    </ItemTitle>
                                </ItemContent>
                            </Item>
                        );
                    })}
                </ItemGroup>
            )}
        </LoginContainer>
    );
}
