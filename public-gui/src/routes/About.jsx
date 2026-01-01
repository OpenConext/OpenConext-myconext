import about from "../assets/about.svg";
import I18n from "../locale/I18n.js";
import "./About.scss";
import {useAppStore} from "../stores/AppStore.js";
import {Background} from "../components/Background.jsx";
import {useEffect} from "react";

export const About = () => {

    const config = useAppStore((state) => state.config);

    useEffect(() => {
        window.scroll(0, 0);
    }, []);

    return (
        <div className="about-container">
            <div className="about">
                <div className="top">
                    <img src={about} className="about" alt="about"/>
                    <div className="top-right">
                        <h1 className="title small">
                            {I18n.t("about.eduID")}
                        </h1>
                        <h2>{I18n.t("about.title")}</h2>
                    </div>
                </div>
            </div>
            <Background>
                <div className="card">
                    <h3>
                        {I18n.t("about.why")}
                    </h3>
                    <p className="info">
                        {I18n.t("about.whyInfo1")}
                    </p>
                    <p className="info">
                        {I18n.t("about.whyInfo2")}
                    </p>
                </div>
                <div className="card bottom">
                    <h3>
                        {I18n.t("about.register")}
                    </h3>
                    <p className="info"
                       dangerouslySetInnerHTML={{__html: I18n.t("about.registerInfo")}}/>
                </div>
                <div className="card bottom">
                    <h3>
                        {I18n.t("about.logins")}
                    </h3>
                    <p className="info"
                       dangerouslySetInnerHTML={{__html: I18n.t("about.loginsInfo")}}/>
                    <ul>
                        <li>
                            <span className="header">{I18n.t("about.institution")}</span><br/>
                            <span className="indented">{I18n.t("about.institutionInfo")}</span>
                        </li>
                        <li>
                            <span className="header">{I18n.t("about.bank")}</span><br/>
                            <span className="indented">{I18n.t("about.bankInfo")}</span>
                        </li>
                        <li>
                            <span className="header">{I18n.t("about.european")}</span><br/>
                            <span className="indented">{I18n.t("about.europeanInfo")}</span>
                        </li>
                    </ul>
                </div>
                <div className="card bottom">
                    <h3>
                        {I18n.t("about.identity")}
                    </h3>
                    <p className="info"
                       dangerouslySetInnerHTML={{__html: I18n.t("about.identityInfo1")}}/>
                    <p className="info"
                       dangerouslySetInnerHTML={{__html: I18n.t("about.identityInfo2")}}/>
                    <ul>
                        <li>
                            <span className="header">{I18n.t("about.magicLink")}</span>
                        </li>
                        <li>
                            <span className="header">{I18n.t("about.password")}</span>
                        </li>
                        <li>
                            <span className="header">{I18n.t("about.passKey")}</span>
                        </li>
                    </ul>
                </div>
                <div className="card bottom">
                    <h3>
                        {I18n.t("about.manage")}
                    </h3>
                    <p className="info"
                       dangerouslySetInnerHTML={{__html: I18n.t("about.manageInfo", {url: config.spBaseUrl})}}/>
                </div>
            </Background>
        </div>
    );
}