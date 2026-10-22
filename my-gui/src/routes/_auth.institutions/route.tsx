import { useState } from 'react';
import { useSearchParams } from 'react-router';

import {
    ArrowRightIcon,
    BuildingOfficeIcon,
    PlusIcon,
} from '@phosphor-icons/react';
import {
    Button,
    Empty,
    EmptyContent,
    EmptyDescription,
    EmptyHeader,
    EmptyMedia,
    EmptyTitle,
} from '@surfnet/curve-react';
import { useMutation, useQuery } from '@tanstack/react-query';

import { fetchConfig, fetchMe, startLinkAccountFlow } from '../../api';
import type { LinkedAccount } from '../../api';
import { DeleteInstitutionDialog } from '../../components/DeleteInstitutionDialog';
import { InstitutionCard } from '../../components/InstitutionCard';
import { NewLinkedAccount } from '../../components/NewLinkedAccount';
import { PageHeader } from '../../components/PageHeader';
import I18n from '../../locale/I18n';
import { accountKey } from '../../utils/verification';
import './route.scss';

export default function Institutions() {
    const { data: me } = useQuery({ queryKey: ['me'], queryFn: fetchMe });
    const { data: config } = useQuery({
        queryKey: ['config'],
        queryFn: fetchConfig,
    });
    const linking = config?.enableAccountLinking !== false;
    const [deleting, setDeleting] = useState<LinkedAccount>();
    const [searchParams] = useSearchParams();
    // the identity page links to the institution it shows
    const open = searchParams.get('open');

    // the browser leaves for the institution to log in
    const { mutate: link, isPending } = useMutation({
        mutationFn: startLinkAccountFlow,
        onSuccess: ({ url }) => window.location.assign(url),
    });

    if (!me) {
        return null;
    }

    const accounts = [
        ...(me.linkedAccounts ?? []),
        ...(me.externalLinkedAccounts ?? []),
    ].sort((a, b) => b.createdAt - a.createdAt);

    return (
        <div className="institutions">
            {accounts.length > 0 && (
                <PageHeader
                    color="purple"
                    icon={<BuildingOfficeIcon />}
                    title={I18n.t('institutions.title')}
                    lead={I18n.t('institutions.lead')}
                />
            )}

            <div className="institutions__card">
                {accounts.length > 0 ? (
                    <>
                        {accounts.map((account) => (
                            <InstitutionCard
                                key={`${account.external}-${account.createdAt}`}
                                account={account}
                                defaultOpen={accountKey(account) === open}
                                onDelete={() => setDeleting(account)}
                            />
                        ))}
                        {linking && (
                            <div className="institutions__footer">
                                <Button
                                    variant="ghost"
                                    className="institutions__link"
                                    disabled={isPending}
                                    onClick={() => link()}
                                >
                                    <PlusIcon />
                                    {I18n.t('institutions.add')}
                                </Button>
                            </div>
                        )}
                    </>
                ) : (
                    <div className="institutions__empty">
                        <Empty>
                            <EmptyHeader>
                                <EmptyMedia variant="icon">
                                    <BuildingOfficeIcon />
                                </EmptyMedia>
                                <EmptyTitle>
                                    {I18n.t('institutions.empty.title')}
                                </EmptyTitle>
                                <EmptyDescription>
                                    {I18n.t('institutions.lead')}
                                </EmptyDescription>
                            </EmptyHeader>
                            <EmptyContent>
                                {linking && (
                                    <Button
                                        disabled={isPending}
                                        onClick={() => link()}
                                    >
                                        <PlusIcon />
                                        {I18n.t('institutions.add')}
                                    </Button>
                                )}
                                <Button
                                    variant="ghost"
                                    className="institutions__link"
                                    render={
                                        <a
                                            href={I18n.t(
                                                'institutions.learnMoreUrl',
                                            )}
                                            target="_blank"
                                            rel="noreferrer"
                                        />
                                    }
                                >
                                    {I18n.t('institutions.learnMore')}
                                    <ArrowRightIcon />
                                </Button>
                            </EmptyContent>
                        </Empty>
                    </div>
                )}
            </div>

            {deleting && (
                <DeleteInstitutionDialog
                    account={deleting}
                    onClose={() => setDeleting(undefined)}
                />
            )}
            <NewLinkedAccount />
        </div>
    );
}
