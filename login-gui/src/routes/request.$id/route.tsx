import type { FC, ReactNode } from 'react';
import { useParams } from 'react-router';

import { LoginContainer } from '../../components';
import I18n from '../../locale/I18n.ts';

const Container: FC<{ children: ReactNode; serviceName?: string }> = ({
    children,
}) => (
    <LoginContainer
        title={'Todo: Create eduID'}
        footer={<div>Todo Footer</div>}
    >
        {children}
    </LoginContainer>
);

export default function Request() {
    const { id } = useParams();

    return <Container>Create new eduID for {id}</Container>;
}
