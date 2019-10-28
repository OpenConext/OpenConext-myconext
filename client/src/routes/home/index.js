import style from './style.css';
import logo from './logo.svg';
import InputElement from '../../components/input';
import I18n from "i18n-js";
import ButtonElement from "../../components/buttonX";
import {Component, createRef} from "preact";

export default class Home extends Component {

    constructor() {
        super();
        this.state = {email: ""};
        this.ref = createRef();
    }

    componentDidMount() {
        if (this.ref && this.ref.current && this.ref.current.base && this.ref.current.base.focus) {
            this.ref.current.base.focus();
        }
    }

    render() {
        const {email} = this.state;
        return (

            <div class={style.home}>
                <div class={style.card}>
                    <div class={style.container}>
                        <img class={style.logo} src={logo} alt="Logo"/>
                        <h2>{I18n.t('login.header')}</h2>
                        <InputElement placeholder={I18n.t('login.emailPlaceholder')}
                                      onChange={e => this.setState({email: e.target.value})}
                                      ref={this.ref}
                                      value={email}/>
                    </div>
                    <div class={style.buttons}>
                        <ButtonElement text={I18n.t('login.next')}/>
                    </div>
                </div>
            </div>
        );

    };
}
