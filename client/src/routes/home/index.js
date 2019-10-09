import {useState} from 'preact/hooks';
import style from './style';
import logo from './logo.svg';
import InputElement from '../../components/input';
import I18n from "i18n-js";
import ButtonElement from "../../components/button";

const Home = () => {
    const [email, setEmail] = useState("");
    return (

        <div class={style.home}>
            <div class={style.card}>
                <div class={style.container}>
                    <img class={style.logo} src={logo} alt="Logo"/>
                    <h2>{I18n.t('login.header')}</h2>
                    <InputElement placeholder={I18n.t('login.emailPlaceholder')}
                                  onChange={e => setEmail(e.target.value)}
                                  value={email}/>
                </div>
                <div class={style.buttons}>
                    <ButtonElement text={I18n.t('login.next')}/>
                </div>
            </div>
        </div>
    );
}

export default Home;
