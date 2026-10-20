import { useState } from 'react';

import { CaretDownIcon, GlobeIcon } from '@phosphor-icons/react';
import {
    Button,
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuRadioGroup,
    DropdownMenuRadioItem,
    DropdownMenuTrigger,
} from '@surfnet/curve-react';

import logo from '../../assets/logo_eduID.svg';
import I18n, { AVAILABLE_LANGUAGES, switchLocale } from '../../locale/I18n';
import styles from './Header.module.scss';

type Language = (typeof AVAILABLE_LANGUAGES)[number];

const isLanguage = (value: unknown): value is Language =>
    AVAILABLE_LANGUAGES.some((lang) => lang === value);

export const Header = () => {
    const [language, setLanguage] = useState<Language>(I18n.locale);

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
                            {AVAILABLE_LANGUAGES.map((lang) => (
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
                <img src={logo} alt="eduID" />
            </div>
        </header>
    );
};
