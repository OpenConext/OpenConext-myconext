import { useNavigate } from 'react-router';

import { WarningIcon } from '@phosphor-icons/react';
import {
    Button,
    Empty,
    EmptyContent,
    EmptyDescription,
    EmptyHeader,
    EmptyMedia,
    EmptyTitle,
} from '@surfnet/curve-react';

import { GuestLayout } from '../../components/GuestLayout';
import I18n from '../../locale/I18n';

export default function CreateFromInstitutionExpired() {
    const navigate = useNavigate();

    return (
        <GuestLayout>
            <Empty>
                <EmptyHeader>
                    <EmptyMedia variant="icon">
                        <WarningIcon />
                    </EmptyMedia>
                    <EmptyTitle>
                        {I18n.t('createFromInstitution.expiredTitle')}
                    </EmptyTitle>
                    <EmptyDescription>
                        {I18n.t('createFromInstitution.expiredInfo')}
                    </EmptyDescription>
                </EmptyHeader>
                <EmptyContent>
                    <Button
                        onClick={() =>
                            void navigate('/create-from-institution')
                        }
                    >
                        {I18n.t('createFromInstitution.back')}
                    </Button>
                </EmptyContent>
            </Empty>
        </GuestLayout>
    );
}
