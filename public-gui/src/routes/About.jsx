import about from "../assets/about.svg";
import I18n from "../locale/I18n.js";
import "./About.scss";
import {Background} from "../components/Background.jsx";
import {useEffect} from "react";
import {InfoLinkField} from "../components/InfoLinkField.jsx";

export const About = () => {

    useEffect(() => {
        window.scroll(0, 0);
    }, []);

    return (
        <div className="about-container">
            <div className="about">
                <div className="top">
                    <img src={about} className="about" alt=""/>
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
                </div>
                <div className="card bottom with-collapse-fields">
                    <h3>
                        {I18n.t("about.register")}
                    </h3>
                    <InfoLinkField title={I18n.t("about.createEduId")}>
                        <>{I18n.t("about.createEduIdInfo")}</>
                    </InfoLinkField>

                </div>
                <div className="card bottom">
                    <h3>
                        {I18n.t("about.logins")}
                    </h3>
                    <p className="info"
                       dangerouslySetInnerHTML={{__html: I18n.t("about.loginsInfo")}}/>
                </div>
            </Background>
        </div>
    );
}
