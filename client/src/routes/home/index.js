import {useState} from 'preact/hooks';
import style from './style';
import logo from './logo.svg';
import InputElement from '../../components/input';
import I18n from "i18n-js";

const Home = () => {
    const [email, setEmail] = useState("");
    return (

        <div class={style.home}>
            <div class={style.card}>
                <div class={style.container}>
                    <img class={style.logo} src={logo} alt="Logo"/>
                    <p>{I18n.t('login.header')}</p>
                    <InputElement placeholder={I18n.t('login.emailPlaceholder')}
                                  onChange={e => setEmail(e.target.value)}
                                  value={email}/>
                </div>
                <button className={style.button}>{I18n.t('login.next')}</button>
            </div>
        </div>
    );
}

export default Home;
