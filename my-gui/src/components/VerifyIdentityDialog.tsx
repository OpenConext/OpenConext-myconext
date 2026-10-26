import { useState } from 'react';

import { BuildingOfficeIcon } from '@phosphor-icons/react';
import {
    Button,
    Dialog,
    DialogContent,
    DialogDescription,
    DialogHeader,
    DialogTitle,
    Item,
    ItemContent,
    ItemGroup,
    ItemMedia,
    ItemTitle,
    Spinner,
} from '@surfnet/curve-react';
import { useMutation, useQuery } from '@tanstack/react-query';

import {
    fetchIdinIssuers,
    startLinkAccountFlow,
    startVerifyAccountFlow,
} from '../api';
import type { User } from '../api';
import eidas from '../assets/identity/eidas.svg';
import idin from '../assets/identity/idin.svg';
import I18n from '../locale/I18n';
import { useConfigStore } from '../store/config';
import { ServiceDeskSteps } from './ServiceDeskSteps';
import './VerifyIdentityDialog.scss';

type View = 'methods' | 'banks' | 'serviceDesk';

type VerifyIdentityDialogProps = {
    user: User;
    initialView?: View;
    open: boolean;
    onOpenChange: (open: boolean) => void;
};

// issuer logos are delivered as inline svg markup
const svgDataUri = (svg: string) =>
    `data:image/svg+xml;utf8,${encodeURIComponent(svg)}`;

type Flow = () => Promise<{ url: string }>;

export const VerifyIdentityDialog = ({
    user,
    initialView = 'methods',
    open,
    onOpenChange,
}: VerifyIdentityDialogProps) => {
    const [view, setView] = useState<View>(initialView);
    const showBanks = view === 'banks';
    const config = useConfigStore((state) => state.config);

    const { data: issuers, isPending: loadingIssuers } = useQuery({
        queryKey: ['idinIssuers'],
        queryFn: fetchIdinIssuers,
        enabled: open && showBanks,
    });

    // the flows redirect the browser to the verification party
    const { mutate: start, isPending } = useMutation({
        mutationFn: (flow: Flow) => flow(),
        onSuccess: ({ url }) => window.location.assign(url),
    });

    const handleOpenChange = (next: boolean) => {
        if (!next) {
            setView('methods');
        }
        onOpenChange(next);
    };

    return (
        <Dialog open={open} onOpenChange={handleOpenChange}>
            <DialogContent className="verify-identity">
                {view === 'serviceDesk' ? (
                    <ServiceDeskSteps
                        user={user}
                        onBack={() => setView('methods')}
                        onClose={() => handleOpenChange(false)}
                    />
                ) : (
                    <>
                        <DialogHeader>
                            <DialogTitle className="verify-identity__title">
                                {showBanks
                                    ? I18n.t('identity.verify.bankTitle')
                                    : I18n.t('identity.verify.title')}
                            </DialogTitle>
                            {showBanks && (
                                <DialogDescription className="verify-identity__description">
                                    {I18n.t('identity.verify.bankDescription')}{' '}
                                    <a
                                        href={I18n.t(
                                            'identity.verify.bankMoreUrl',
                                        )}
                                        target="_blank"
                                        rel="noreferrer"
                                    >
                                        {I18n.t('identity.verify.bankMore')}
                                    </a>
                                </DialogDescription>
                            )}
                        </DialogHeader>
                        {showBanks ? (
                            <>
                                {loadingIssuers ? (
                                    <Spinner
                                        aria-label={I18n.t('common.loading')}
                                    />
                                ) : (
                                    <ItemGroup className="verify-identity__list">
                                        {(issuers ?? []).map((issuer) => (
                                            <Item
                                                key={issuer.id}
                                                render={
                                                    <button
                                                        type="button"
                                                        disabled={isPending}
                                                    />
                                                }
                                                onClick={() =>
                                                    start(() =>
                                                        startVerifyAccountFlow(
                                                            'idin',
                                                            issuer.id,
                                                        ),
                                                    )
                                                }
                                            >
                                                <ItemMedia className="verify-identity__logo">
                                                    {issuer.logo && (
                                                        <img
                                                            src={svgDataUri(
                                                                issuer.logo,
                                                            )}
                                                            alt=""
                                                        />
                                                    )}
                                                </ItemMedia>
                                                <ItemContent>
                                                    <ItemTitle className="verify-identity__option">
                                                        {issuer.name}
                                                    </ItemTitle>
                                                </ItemContent>
                                            </Item>
                                        ))}
                                    </ItemGroup>
                                )}
                                <div className="verify-identity__footer">
                                    <p>
                                        {I18n.t('identity.verify.bankMissing')}
                                    </p>
                                    <button
                                        type="button"
                                        className="verify-identity__footer-link"
                                        onClick={() => setView('methods')}
                                    >
                                        {I18n.t('identity.verify.otherMethod')}
                                    </button>
                                </div>
                            </>
                        ) : (
                            <>
                                <div className="verify-identity__section">
                                    <p className="verify-identity__text">
                                        {I18n.t(
                                            'identity.verify.institutionQuestion',
                                        )}
                                    </p>
                                    <Button
                                        disabled={isPending}
                                        onClick={() =>
                                            start(startLinkAccountFlow)
                                        }
                                    >
                                        <BuildingOfficeIcon />
                                        {I18n.t('identity.verify.institution')}
                                    </Button>
                                </div>
                                {config?.featureIdVerify && (
                                    <div className="verify-identity__section">
                                        <p className="verify-identity__text">
                                            {I18n.t(
                                                'identity.verify.otherMethods',
                                            )}
                                        </p>
                                        <ItemGroup className="verify-identity__list">
                                            <Item
                                                render={
                                                    <button type="button" />
                                                }
                                                onClick={() => setView('banks')}
                                            >
                                                <ItemMedia>
                                                    <img
                                                        src={idin}
                                                        alt=""
                                                        width={24}
                                                        height={24}
                                                    />
                                                </ItemMedia>
                                                <ItemContent>
                                                    <ItemTitle className="verify-identity__option">
                                                        {I18n.t(
                                                            'identity.verify.bank',
                                                        )}
                                                    </ItemTitle>
                                                </ItemContent>
                                            </Item>
                                            <Item
                                                render={
                                                    <button
                                                        type="button"
                                                        disabled={isPending}
                                                    />
                                                }
                                                onClick={() =>
                                                    start(() =>
                                                        startVerifyAccountFlow(
                                                            'eherkenning',
                                                        ),
                                                    )
                                                }
                                            >
                                                <ItemMedia>
                                                    <img
                                                        src={eidas}
                                                        alt=""
                                                        width={24}
                                                        height={24}
                                                    />
                                                </ItemMedia>
                                                <ItemContent>
                                                    <ItemTitle className="verify-identity__option">
                                                        {I18n.t(
                                                            'identity.verify.european',
                                                        )}
                                                    </ItemTitle>
                                                </ItemContent>
                                            </Item>
                                        </ItemGroup>
                                    </div>
                                )}
                                {config?.featureServiceDeskActive && (
                                    <div className="verify-identity__footer">
                                        <p>
                                            {I18n.t(
                                                'identity.verify.noOtherMethod',
                                            )}
                                        </p>
                                        <button
                                            type="button"
                                            className="verify-identity__footer-link"
                                            onClick={() =>
                                                setView('serviceDesk')
                                            }
                                        >
                                            {I18n.t(
                                                'identity.verify.serviceDesk',
                                            )}
                                        </button>
                                    </div>
                                )}
                            </>
                        )}
                    </>
                )}
            </DialogContent>
        </Dialog>
    );
};
