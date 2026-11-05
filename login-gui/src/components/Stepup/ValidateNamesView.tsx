import { BuildingOfficeIcon } from '@phosphor-icons/react';
import {
    Button,
    Item,
    ItemContent,
    ItemGroup,
    ItemMedia,
    ItemTitle,
} from '@surfnet/curve-react';

import eidasLogo from '../../assets/eidas.svg';
import idinLogo from '../../assets/idin.svg';
import I18n from '../../locale/I18n.ts';
import { LoginContainer } from '../LoginContainer/LoginContainer';
import { ValidateNamesMode } from './ValidateNamesMode';
import styles from './ValidateNamesView.module.scss';

interface ValidateNamesViewProps {
    onUseInstitution: () => void;
    mode?: ValidateNamesMode;
}

// TODO: wire up the bank (iDIN: /myconext/api/idp/verify/link/{id}?idpScoping=idin&bankId=..),
// European ID (idpScoping=eherkenning) and service desk handlers.
export const ValidateNamesView = ({
    onUseInstitution,
    mode = ValidateNamesMode.All,
}: ValidateNamesViewProps) => {
    const showInstitution = mode !== ValidateNamesMode.AlternativesOnly;
    const showAlternatives = mode !== ValidateNamesMode.InstitutionOnly;

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
            {showInstitution && (
                <Button type="button" onClick={onUseInstitution} autoFocus>
                    <BuildingOfficeIcon aria-hidden data-icon="inline-start" />
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
                    <ItemGroup className={styles.list}>
                        <Item
                            className={styles.item}
                            render={<button type="button" />}
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
                                    {I18n.t('stepup.proveIdentity.bank')}
                                </ItemTitle>
                            </ItemContent>
                        </Item>
                        <Item
                            className={styles.item}
                            render={<button type="button" />}
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
                                    {I18n.t('stepup.proveIdentity.european')}
                                </ItemTitle>
                            </ItemContent>
                        </Item>
                    </ItemGroup>
                </div>
            )}
        </LoginContainer>
    );
};
