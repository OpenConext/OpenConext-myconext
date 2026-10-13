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

import logo from '../assets/logo_eduID.svg';
import styles from './Header.module.scss';

const LANGUAGES = ['NL', 'EN'] as const;

type Language = (typeof LANGUAGES)[number];

const isLanguage = (value: unknown): value is Language =>
    LANGUAGES.some((lang) => lang === value);

export const Header = () => {
    const [language, setLanguage] = useState<Language>('NL');

    return (
        <header className={styles.header}>
            <div className={styles.language}>
                <DropdownMenu>
                    <DropdownMenuTrigger
                        render={
                            <Button aria-label="Language">
                                <GlobeIcon aria-hidden />
                                {language}
                                <CaretDownIcon aria-hidden />
                            </Button>
                        }
                    />
                    <DropdownMenuContent align="end">
                        <DropdownMenuRadioGroup
                            value={language}
                            onValueChange={(value) =>
                                isLanguage(value) && setLanguage(value)
                            }
                        >
                            {LANGUAGES.map((lang) => (
                                <DropdownMenuRadioItem key={lang} value={lang}>
                                    {lang}
                                </DropdownMenuRadioItem>
                            ))}
                        </DropdownMenuRadioGroup>
                    </DropdownMenuContent>
                </DropdownMenu>
            </div>
            <div className={styles.logo}>
                <img src={logo} alt="eduID" />
            </div>
        </header>
    );
};
