import type { MouseEvent } from 'react';

import I18n, { AVAILABLE_LANGUAGES, switchLocale } from '../locale/I18n';
import { stopEvent } from '../utils/Utils';
import './LanguageSelector.scss';

export const LanguageSelector = () => {
    const renderLocaleChooser = (locale: string) => (
        <a
            href="locale"
            className={I18n.locale === locale ? 'is-active' : 'not-active'}
            title={I18n.t('footer.select_locale')}
            onClick={(e: MouseEvent) => {
                stopEvent(e);
                void switchLocale(locale);
            }}
        >
            {I18n.translations[locale].code as string}
        </a>
    );

    return (
        <nav className="language-switcher" aria-label="Language">
            <ul>
                {AVAILABLE_LANGUAGES.map((locale, index) => (
                    <li key={locale}>
                        {renderLocaleChooser(locale)}
                        {index < AVAILABLE_LANGUAGES.length - 1 && (
                            <span className="divider">|</span>
                        )}
                    </li>
                ))}
            </ul>
        </nav>
    );
};
