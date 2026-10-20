import surfLogo from '../assets/surf-logo.svg';
import I18n from '../locale/I18n';
import './PageFooter.scss';

export const PageFooter = () => (
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
            <img
                src={surfLogo}
                alt={I18n.t('logo.surf')}
                width={60}
                height={30}
            />
        </div>
    </footer>
);
