import {
    ArrowClockwiseIcon,
    IdentificationCardIcon,
} from '@phosphor-icons/react';
import { Button } from '@surfnet/curve-react';

import HighFive from '../icons/undraw_High_five.svg';
import I18n from '../locale/I18n';
import './Confirmation.scss';

const Confirmation = ({ restart }: { restart: () => void }) => (
    <section className="confirmation">
        <div className="confirmation__text">
            <h2>{I18n.t('confirmation.header')}</h2>
            <p>{I18n.t('confirmation.info')}</p>
        </div>
        <div className="confirmation__image">
            <HighFive />
        </div>
        <div className="confirmation__actions">
            {/* TODO: the action of this button is implemented later */}
            <Button variant="outline">
                <IdentificationCardIcon />
                {I18n.t('confirmation.viewEduId')}
            </Button>
            <Button variant="outline" onClick={restart}>
                <ArrowClockwiseIcon />
                {I18n.t('confirmation.restart')}
            </Button>
        </div>
    </section>
);

export default Confirmation;
