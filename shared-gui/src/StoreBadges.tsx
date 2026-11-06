import './StoreBadges.scss';
import badges from './assets/store-badges.png';

export type StoreBadgesProps = {
    googleUrl: string;
    appleUrl: string;
    googleLabel: string;
    appleLabel: string;
};

export const StoreBadges = ({
    googleUrl,
    appleUrl,
    googleLabel,
    appleLabel,
}: StoreBadgesProps) => (
    <div className="store-badges">
        <a
            className="store-badges__badge store-badges__badge--google"
            href={googleUrl}
            target="_blank"
            rel="noreferrer"
            aria-label={googleLabel}
        >
            <img src={badges} alt="" />
        </a>
        <a
            className="store-badges__badge store-badges__badge--apple"
            href={appleUrl}
            target="_blank"
            rel="noreferrer"
            aria-label={appleLabel}
        >
            <img src={badges} alt="" />
        </a>
    </div>
);
