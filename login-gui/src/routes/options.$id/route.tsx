import type { ComponentType } from 'react';
import { Navigate, useLocation, useNavigate, useParams } from 'react-router';

import {
    DeviceMobileIcon,
    EnvelopeIcon,
    KeyIcon,
    PasswordIcon,
} from '@phosphor-icons/react';
import type { IconProps } from '@phosphor-icons/react';
import { useQuery } from '@tanstack/react-query';

import { fetchLoginMethods } from '../../api';
import {
    ErrorAlert,
    ItemList,
    LoadingSpinner,
    LoginContainer,
} from '../../components';
import I18n from '../../locale/I18n';
import {
    type LoginMethod,
    isLoginMethod,
    loginMethodPath,
} from '../../utils/loginMethods';

const OPTION_ICONS: Record<LoginMethod, ComponentType<IconProps>> = {
    useApp: DeviceMobileIcon,
    useWebAuthn: KeyIcon,
    useCode: EnvelopeIcon,
    usePassword: PasswordIcon,
};

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

    const options = methods?.filter(isLoginMethod) ?? [];

    const selectOption = (option: LoginMethod) => {
        navigate(loginMethodPath(option, id), { state: { email } });
    };

    return (
        <LoginContainer
            title={I18n.t('options.title')}
            description={I18n.t('options.description')}
            onBack={() => navigate(`/login/${id}`)}
            backLabel={email}
        >
            {isPending && <LoadingSpinner />}
            {isError && <ErrorAlert>{I18n.t('options.error')}</ErrorAlert>}
            {methods && (
                <ItemList
                    items={options.map((option) => ({
                        key: option,
                        icon: OPTION_ICONS[option],
                        label: I18n.t(`options.${option}`),
                        onClick: () => selectOption(option),
                    }))}
                />
            )}
        </LoginContainer>
    );
}
