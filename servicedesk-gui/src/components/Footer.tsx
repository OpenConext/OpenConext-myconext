import surfLogo from '../icons/surf-logo.svg?url';
import I18n from '../locale/I18n';
import { useAppStore } from '../stores/AppStore';
import './Footer.scss';
import { LanguageSelector } from './LanguageSelector';

export const Footer = () => {
    // members switch language in the user menu, which others don't have
    const isMember = useAppStore((state) =>
        Boolean(state.user?.serviceDeskMember),
    );

    return (
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
                    {!isMember && <LanguageSelector />}
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
};
