import { useState } from 'react';

import { OverridableComponent } from '@myconext/shared-gui';
import { CaretDownIcon, GlobeIcon } from '@phosphor-icons/react';
import { useQuery } from '@tanstack/react-query';
import {
    Button,
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuRadioGroup,
    DropdownMenuRadioItem,
    DropdownMenuTrigger,
} from '@surfnet/curve-react';

import { configQueryOptions } from '../../api';
import logo from '../../assets/logo-surf.svg';
import I18n, { AVAILABLE_LANGUAGES, switchLocale } from '../../locale/I18n';
import styles from './Header.module.scss';

type Language = (typeof AVAILABLE_LANGUAGES)[number];

const isLanguage = (value: unknown): value is Language =>
    AVAILABLE_LANGUAGES.some((lang) => lang === value);

export const Header = () => {
    const [language, setLanguage] = useState<Language>(I18n.locale);
    const { data: config } = useQuery(configQueryOptions);
    const languages = AVAILABLE_LANGUAGES.filter((lang) =>
        (config?.supportedLanguages ?? '').split(',').includes(lang),
    );

    const handleLanguageChange = (updatedLanguage: string) => {
        if (isLanguage(updatedLanguage)) {
            setLanguage(updatedLanguage);
            switchLocale(updatedLanguage);
        }
    };

    return (
        <header className={styles.header} data-testid="app-header">
            <div className={styles.language} data-testid="language-selector">
                <DropdownMenu>
                    <DropdownMenuTrigger
                        render={
                            <Button
                                aria-label="Language"
                                data-testid="dropdown-trigger"
                            >
                                <GlobeIcon
                                    aria-hidden
                                    data-icon="inline-start"
                                />
                                {language.toUpperCase()}
                                <CaretDownIcon
                                    aria-hidden
                                    data-icon="inline-end"
                                />
                            </Button>
                        }
                    />
                    <DropdownMenuContent
                        align="end"
                        data-testid="dropdown-content"
                    >
                        <DropdownMenuRadioGroup
                            value={language}
                            data-testid="languages"
                            onValueChange={(value) =>
                                handleLanguageChange(value)
                            }
                        >
                            {languages.map((lang) => (
                                <DropdownMenuRadioItem
                                    key={lang}
                                    value={lang}
                                    data-testid={`language-${lang}`}
                                >
                                    {lang.toUpperCase()}
                                </DropdownMenuRadioItem>
                            ))}
                        </DropdownMenuRadioGroup>
                    </DropdownMenuContent>
                </DropdownMenu>
            </div>

            <div className={styles.logo} data-testid="logo">
                <OverridableComponent appCustomizationReactNodeKey="appLogo">
                    <img src={logo} alt="SURF" />
                </OverridableComponent>
            </div>
        </header>
    );
};
