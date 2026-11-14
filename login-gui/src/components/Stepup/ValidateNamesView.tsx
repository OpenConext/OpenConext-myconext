import { useState } from 'react';

import { BuildingOfficeIcon } from '@phosphor-icons/react';
import {
    Button,
    Item,
    ItemContent,
    ItemGroup,
    ItemMedia,
    ItemTitle,
} from '@surfnet/curve-react';

import { verifyLinkUrl } from '../../api';
import eidasLogo from '../../assets/eidas.svg';
import idinLogo from '../../assets/idin.svg';
import I18n from '../../locale/I18n.ts';
import { handleArrowKeyNavigation } from '../ItemList/ItemList';
import { Spinner } from '../Loading/Spinner';
import { LoginContainer } from '../LoginContainer/LoginContainer';
import { BankSelectView } from './BankSelectView';
import { ValidateNamesMode } from './ValidateNamesMode';
import styles from './ValidateNamesView.module.scss';

interface ValidateNamesViewProps {
    id: string;
    onUseInstitution: () => void;
    mode?: ValidateNamesMode;
}

// TODO: wire up the service desk handler.
export const ValidateNamesView = ({
    id,
    onUseInstitution,
    mode = ValidateNamesMode.All,
}: ValidateNamesViewProps) => {
    const [showBanks, setShowBanks] = useState(false);
    // The browser leaves the page on redirect; keep the buttons disabled meanwhile.
    const [redirecting, setRedirecting] = useState(false);

    const showInstitution = mode !== ValidateNamesMode.AlternativesOnly;
    const showAlternatives = mode !== ValidateNamesMode.InstitutionOnly;

    const redirect = (url: string) => {
        setRedirecting(true);
        window.location.href = url;
    };

    if (showBanks) {
        return (
            <BankSelectView
                onSelectBank={(bankId) =>
                    redirect(verifyLinkUrl(id, 'idin', bankId))
                }
                onBack={() => setShowBanks(false)}
                isRedirecting={redirecting}
            />
        );
    }

    return (
        <LoginContainer
            title={I18n.t('stepup.proveIdentity.title')}
            // The description asks about an institution account, so it only fits when that option is offered.
            description={
                showInstitution
                    ? I18n.t('stepup.proveIdentity.description')
                    : undefined
            }
            headerAlign="start"
            footer={
                <>
                    <p>{I18n.t('stepup.proveIdentity.footerQuestion')}</p>
                    <a href="#" className={styles.footerLink}>
                        {I18n.t('stepup.proveIdentity.serviceDesk')}
                    </a>
                </>
            }
        >
            <div className={styles.tiles} onKeyDown={handleArrowKeyNavigation}>
                {showInstitution && (
                    <Button
                        type="button"
                        disabled={redirecting}
                        onClick={() => {
                            setRedirecting(true);
                            onUseInstitution();
                        }}
                        autoFocus
                    >
                        <BuildingOfficeIcon
                            aria-hidden
                            data-icon="inline-start"
                        />
                        {I18n.t('stepup.proveIdentity.institution')}
                    </Button>
                )}
                {showAlternatives && (
                    <div className={styles.alternatives}>
                        {showInstitution && (
                            <p className={styles.hint}>
                                {I18n.t('stepup.proveIdentity.alternatives')}
                            </p>
                        )}
                        {redirecting ? (
                            <Spinner />
                        ) : (
                            <ItemGroup className={styles.list}>
                                <Item
                                    className={styles.item}
                                    render={
                                        <button
                                            type="button"
                                            // The institution button holds the initial focus when shown.
                                            autoFocus={!showInstitution}
                                        />
                                    }
                                    onClick={() => setShowBanks(true)}
                                >
                                    <ItemMedia>
                                        <img
                                            src={idinLogo}
                                            alt=""
                                            width={24}
                                            height={24}
                                        />
                                    </ItemMedia>
                                    <ItemContent>
                                        <ItemTitle>
                                            {I18n.t(
                                                'stepup.proveIdentity.bank',
                                            )}
                                        </ItemTitle>
                                    </ItemContent>
                                </Item>
                                <Item
                                    className={styles.item}
                                    render={<button type="button" />}
                                    onClick={() =>
                                        redirect(
                                            verifyLinkUrl(id, 'eherkenning'),
                                        )
                                    }
                                >
                                    <ItemMedia>
                                        <img
                                            src={eidasLogo}
                                            alt=""
                                            width={24}
                                            height={24}
                                        />
                                    </ItemMedia>
                                    <ItemContent>
                                        <ItemTitle>
                                            {I18n.t(
                                                'stepup.proveIdentity.european',
                                            )}
                                        </ItemTitle>
                                    </ItemContent>
                                </Item>
                            </ItemGroup>
                        )}
                    </div>
                )}
            </div>
        </LoginContainer>
    );
};
