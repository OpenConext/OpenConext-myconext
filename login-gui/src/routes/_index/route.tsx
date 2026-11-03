import phone from '../../assets/phone.svg';
import { LoginContainer } from '../../components';
import I18n from '../../locale/I18n';
import styles from './route.module.scss';

export default function Home() {
    return (
        <LoginContainer
            title={I18n.t('landing.title')}
            description={I18n.t('landing.description')}
        >
            <img
                className={styles.image}
                src={phone}
                alt={I18n.t('landing.imageAlt')}
            />
            <p>{I18n.t('landing.services')}</p>
            <p>{I18n.t('landing.career')}</p>
            <p>{I18n.t('landing.control')}</p>
            <p className={styles.more}>
                <a href="https://eduid.nl/home">{I18n.t('landing.more')}</a>
            </p>
        </LoginContainer>
    );
}
