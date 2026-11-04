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
import { LoginContainer } from '../../components';
import I18n from '../../locale/I18n.ts';
import { ValidateNamesMode } from './ValidateNamesMode';
import styles from './ValidateNamesView.module.scss';

interface ValidateNamesViewProps {
    onUseInstitution: () => void;
    mode?: ValidateNamesMode;
}

// The other handlers are wired up once the flow logic is added.
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
                <Button type="button" onClick={onUseInstitution}>
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
