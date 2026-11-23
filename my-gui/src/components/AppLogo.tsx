import { OverridableComponent } from '@myconext/shared-gui';

import surfLogo from '../assets/surf-logo.svg';
import I18n from '../locale/I18n';
import './AppLogo.scss';

type AppLogoProps = {
    width: number;
    height: number;
};

export const AppLogo = ({ width, height }: AppLogoProps) => (
    <span className="app-logo" style={{ width, height }}>
        <OverridableComponent appCustomizationReactNodeKey="appLogo">
            <img
                src={surfLogo}
                alt={I18n.t('logo.surf')}
                width={width}
                height={height}
            />
        </OverridableComponent>
    </span>
);
