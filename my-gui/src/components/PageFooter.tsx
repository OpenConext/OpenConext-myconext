import { CaretDownIcon } from '@phosphor-icons/react';
import {
    Button,
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from '@surfnet/curve-react';
import { useQuery } from '@tanstack/react-query';

import { fetchConfig, updateLanguage } from '../api';
import surfLogo from '../assets/surf-logo.svg';
import I18n, { AVAILABLE_LANGUAGES, switchLocale } from '../locale/I18n';
import './PageFooter.scss';

export const PageFooter = () => {
    const { data: config } = useQuery({
        queryKey: ['config'],
        queryFn: fetchConfig,
    });
    const baseDomain =
        typeof config?.baseDomain === 'string' ? config.baseDomain : undefined;
    const languages = AVAILABLE_LANGUAGES.filter((lang) =>
        (config?.supportedLanguages ?? '').split(',').includes(lang),
    );

    return (
        <footer className="page-footer">
            <div className="page-footer__content">
                <nav className="page-footer__links">
                    <a
                        href={I18n.t('footer.privacyUrl')}
                        target="_blank"
                        rel="noreferrer"
                    >
                        {I18n.t('footer.privacy')}
                    </a>
                    <a
                        href={I18n.t('footer.aboutUrl')}
                        target="_blank"
                        rel="noreferrer"
                    >
                        {I18n.t('footer.about')}
                    </a>
                </nav>
                <div className="page-footer__language">
                    <DropdownMenu>
                        <DropdownMenuTrigger
                            render={<Button variant="ghost" />}
                        >
                            {I18n.t('code')}
                            <CaretDownIcon />
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="center">
                            {languages.map((lang) => (
                                <DropdownMenuItem
                                    key={lang}
                                    onClick={() =>
                                        switchLocale(
                                            lang,
                                            baseDomain,
                                            updateLanguage,
                                        )
                                    }
                                >
                                    {I18n.t(`languages.${lang}`)}
                                </DropdownMenuItem>
                            ))}
                        </DropdownMenuContent>
                    </DropdownMenu>
                </div>
                <img
                    className="page-footer__logo"
                    src={surfLogo}
                    alt={I18n.t('logo.surf')}
                    width={60}
                    height={30}
                />
            </div>
        </footer>
    );
};
