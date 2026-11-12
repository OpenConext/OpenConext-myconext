import surfLogo from '../icons/surf-logo.svg?url';
import I18n from '../locale/I18n';
import './Footer.scss';
import { LanguageSelector } from './LanguageSelector';

export const Footer = () => (
    <footer className="footer">
        <div className="footer-page">
            <div className="footer-inner">
                <nav className="menu">
                    <ul>
                        <li>
                            <a
                                href={I18n.t('footer.termsLink')}
                                target="_blank"
                                rel="noopener noreferrer"
                            >
                                <span>{I18n.t('footer.terms')}</span>
                            </a>
                        </li>
                        <li>
                            <a
                                href={I18n.t('footer.privacyLink')}
                                target="_blank"
                                rel="noopener noreferrer"
                            >
                                <span>{I18n.t('footer.privacy')}</span>
                            </a>
                        </li>
                    </ul>
                </nav>
                <LanguageSelector />
                <div className="branding">
                    <a
                        href={I18n.t('footer.surfLink')}
                        target="_blank"
                        rel="noopener noreferrer"
                    >
                        <img src={surfLogo} alt="SURF" />
                    </a>
                </div>
            </div>
        </div>
    </footer>
);
