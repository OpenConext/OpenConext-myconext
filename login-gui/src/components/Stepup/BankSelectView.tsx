import {
    Item,
    ItemContent,
    ItemGroup,
    ItemMedia,
    ItemTitle,
    Spinner,
} from '@surfnet/curve-react';
import { useQuery } from '@tanstack/react-query';

import { idinIssuersQueryOptions } from '../../api';
import I18n from '../../locale/I18n.ts';
import { handleArrowKeyNavigation } from '../ItemList/ItemList';
import { LoginContainer } from '../LoginContainer/LoginContainer';
import styles from './ValidateNamesView.module.scss';

interface BankSelectViewProps {
    onSelectBank: (bankId: string) => void;
    onBack: () => void;
    disabled?: boolean;
}

// Issuer logos are delivered as inline svg markup.
const svgDataUri = (svg: string): string =>
    `data:image/svg+xml;utf8,${encodeURIComponent(svg)}`;

export const BankSelectView = ({
    onSelectBank,
    onBack,
    disabled = false,
}: BankSelectViewProps) => {
    const { data: issuers, isPending } = useQuery(idinIssuersQueryOptions);

    return (
        <LoginContainer
            title={I18n.t('stepup.proveIdentity.bankTitle')}
            description={I18n.t('stepup.proveIdentity.bankDescription')}
            headerAlign="start"
            onBack={onBack}
            backLabel={I18n.t('stepup.proveIdentity.back')}
            footer={
                <>
                    <p>{I18n.t('stepup.proveIdentity.bankMissing')}</p>
                    <button
                        type="button"
                        className={styles.footerLink}
                        onClick={onBack}
                    >
                        {I18n.t('stepup.proveIdentity.otherMethod')}
                    </button>
                </>
            }
        >
            {isPending ? (
                <Spinner
                    aria-label={I18n.t('stepup.proveIdentity.bankTitle')}
                />
            ) : (
                <ItemGroup
                    className={styles.list}
                    onKeyDown={handleArrowKeyNavigation}
                >
                    {(issuers ?? []).map((issuer, index) => (
                        <Item
                            key={issuer.id}
                            className={styles.item}
                            render={
                                <button
                                    type="button"
                                    disabled={disabled}
                                    // Buttons mount after the issuers load, so this focuses once they appear.
                                    autoFocus={index === 0}
                                />
                            }
                            onClick={() => onSelectBank(issuer.id)}
                        >
                            <ItemMedia>
                                {issuer.logo && (
                                    <img
                                        src={svgDataUri(issuer.logo)}
                                        alt=""
                                        width={24}
                                        height={24}
                                    />
                                )}
                            </ItemMedia>
                            <ItemContent>
                                <ItemTitle>{issuer.name}</ItemTitle>
                            </ItemContent>
                        </Item>
                    ))}
                </ItemGroup>
            )}
        </LoginContainer>
    );
};
