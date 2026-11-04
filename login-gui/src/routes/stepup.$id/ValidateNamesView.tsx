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
import styles from './ValidateNamesView.module.scss';

interface ValidateNamesViewProps {
    onUseInstitution: () => void;
}

// The other handlers are wired up once the flow logic is added.
export const ValidateNamesView = ({
    onUseInstitution,
}: ValidateNamesViewProps) => (
    <LoginContainer
        title={I18n.t('stepup.proveIdentity.title')}
        description={I18n.t('stepup.proveIdentity.description')}
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
        <Button type="button" onClick={onUseInstitution}>
            <BuildingOfficeIcon aria-hidden data-icon="inline-start" />
            {I18n.t('stepup.proveIdentity.institution')}
        </Button>
        <div className={styles.alternatives}>
            <p className={styles.hint}>
                {I18n.t('stepup.proveIdentity.alternatives')}
            </p>
            <ItemGroup className={styles.list}>
                <Item className={styles.item} render={<button type="button" />}>
                    <ItemMedia>
                        <img src={idinLogo} alt="" width={24} height={24} />
                    </ItemMedia>
                    <ItemContent>
                        <ItemTitle>
                            {I18n.t('stepup.proveIdentity.bank')}
                        </ItemTitle>
                    </ItemContent>
                </Item>
                <Item className={styles.item} render={<button type="button" />}>
                    <ItemMedia>
                        <img src={eidasLogo} alt="" width={24} height={24} />
                    </ItemMedia>
                    <ItemContent>
                        <ItemTitle>
                            {I18n.t('stepup.proveIdentity.european')}
                        </ItemTitle>
                    </ItemContent>
                </Item>
            </ItemGroup>
        </div>
    </LoginContainer>
);
