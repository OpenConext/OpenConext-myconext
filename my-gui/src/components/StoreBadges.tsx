import badges from '../assets/app/store-badges.png';
import I18n from '../locale/I18n';
import './StoreBadges.scss';

// the Figma design has both store badges in one image, shown cropped
export const StoreBadges = () => (
    <div className="store-badges">
        <a
            className="store-badges__badge store-badges__badge--google"
            href={I18n.t('app.get.google')}
            target="_blank"
            rel="noreferrer"
            aria-label="Google Play"
        >
            <img src={badges} alt="" />
        </a>
        <a
            className="store-badges__badge store-badges__badge--apple"
            href={I18n.t('app.get.apple')}
            target="_blank"
            rel="noreferrer"
            aria-label="App Store"
        >
            <img src={badges} alt="" />
        </a>
    </div>
);
