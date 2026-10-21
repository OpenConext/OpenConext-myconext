import { useNavigate, useSearchParams } from 'react-router';

import { LinkBreakIcon } from '@phosphor-icons/react';
import {
    Button,
    Empty,
    EmptyContent,
    EmptyDescription,
    EmptyHeader,
    EmptyMedia,
    EmptyTitle,
    Spinner,
} from '@surfnet/curve-react';
import { useMutation } from '@tanstack/react-query';

import { startCreateFromInstitutionFlow, startLinkAccountFlow } from '../api';
import I18n from '../locale/I18n';
import './LinkErrorPage.scss';

export type LinkErrorVariant =
    | 'eppnAlreadyLinked'
    | 'attributeMissing'
    | 'subjectAlreadyLinked'
    | 'externalAccountLinkedError';

type LinkErrorPageProps = {
    variant: LinkErrorVariant;
};

// the server redirects here when linking an institution or verifying an
// identity failed; `fromInstitution` marks the flow to create an eduID
export const LinkErrorPage = ({ variant }: LinkErrorPageProps) => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const email = searchParams.get('email') ?? '';
    const fromInstitution = searchParams.has('fromInstitution');
    const description = searchParams.get('error_description');

    // the browser leaves for the institution to log in again
    const retry = useMutation({
        mutationFn: () =>
            fromInstitution
                ? startCreateFromInstitutionFlow(true)
                : startLinkAccountFlow(),
        onSuccess: ({ url }) => window.location.assign(url),
    });

    const linkingFlow =
        variant === 'eppnAlreadyLinked' || variant === 'attributeMissing';
    const info = I18n.t(
        `linkError.${variant}.${fromInstitution ? 'infoNew' : 'info'}`,
        { email, defaultValue: I18n.t(`linkError.${variant}.info`, { email }) },
    );

    const handleRetry = () => {
        if (linkingFlow) {
            retry.mutate();
        } else {
            // the identity page reopens the verification options
            void navigate('/identity?retry=true');
        }
    };

    return (
        <div className="link-error">
            <Empty>
                <EmptyHeader>
                    <EmptyMedia variant="icon">
                        <LinkBreakIcon />
                    </EmptyMedia>
                    <EmptyTitle>
                        {I18n.t(`linkError.${variant}.title`)}
                    </EmptyTitle>
                    <EmptyDescription>{info}</EmptyDescription>
                    {description && (
                        <EmptyDescription>{description}</EmptyDescription>
                    )}
                </EmptyHeader>
                <EmptyContent>
                    <Button disabled={retry.isPending} onClick={handleRetry}>
                        {retry.isPending && <Spinner />}
                        {I18n.t('linkError.retry')}
                    </Button>
                </EmptyContent>
            </Empty>
        </div>
    );
};
