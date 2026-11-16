import { useNavigate } from 'react-router-dom';

import {
    ArrowClockwiseIcon,
    IdentificationCardIcon,
} from '@phosphor-icons/react';
import { Button } from '@surfnet/curve-react';

import HighFive from '../icons/undraw_High_five.svg';
import I18n from '../locale/I18n';
import { useAppStore } from '../stores/AppStore';
import './Confirmation.scss';

const Confirmation = ({ restart }: { restart: () => void }) => {
    const navigate = useNavigate();
    const userId = useAppStore((state) => state.controlCode.userId);

    return (
        <section className="confirmation">
            <div className="confirmation__text">
                <h2>{I18n.t('confirmation.header')}</h2>
                <p>{I18n.t('confirmation.info')}</p>
            </div>
            <div className="confirmation__image">
                <HighFive />
            </div>
            <div className="confirmation__actions">
                <Button
                    variant="outline"
                    disabled={!userId}
                    onClick={() => navigate(`/users/${userId}`)}
                >
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
};

export default Confirmation;
