import about from "../assets/about.svg";
import I18n from "../locale/I18n.js";
import "./About.scss";
import {CollapseField} from "../components/CollapseField.jsx";
import {useAppStore} from "../stores/AppStore.js";

export const About = () => {

    const config = useAppStore((state) => state.config);

    return (
        <div className="about-container">
            <div className="about">
                <div className="top">
                    <img src={about} className="about" alt="about"/>
                    <div className="top-right">
                        <h1 className="title">
                            {I18n.t("about.eduID")}
                        </h1>
                        <h3>{I18n.t("about.title")}</h3>
                    </div>
                </div>
            </div>
            <div className="background">
                <div className="card">
                    <h5>
                        {I18n.t("about.why")}
                    </h5>
                    <p className="info">
                        {I18n.t("about.whyInfo")}
                    </p>
                </div>
                <div className="card bottom">
                    <h5>
                        {I18n.t("about.register")}
                    </h5>
                    <p className="info"
                       dangerouslySetInnerHTML={{__html: I18n.t("about.registerInfo")}}/>
                </div>
                <div className="card bottom">
                    <h5>
                        {I18n.t("about.logins")}
                    </h5>
                    <p className="info"
                       dangerouslySetInnerHTML={{__html: I18n.t("about.loginsInfo")}}/>
                    <CollapseField title={I18n.t("about.magicLink")}
                                   info={I18n.t("about.magicLinkInfo")}/>
                    <CollapseField title={I18n.t("about.password")}
                                   info={I18n.t("about.passwordInfo")}/>
                    <CollapseField title={I18n.t("about.passKey")}
                                   info={I18n.t("about.passKeyInfo")}/>
                </div>
                <div className="card bottom">
                    <h5>
                        {I18n.t("about.identity")}
                    </h5>
                    <p className="info"
                       dangerouslySetInnerHTML={{__html: I18n.t("about.identityInfo")}}/>
                    <CollapseField title={I18n.t("about.institution")}
                                   info={I18n.t("about.institutionInfo")}/>
                    <CollapseField title={I18n.t("about.bank")}
                                   info={I18n.t("about.bankInfo")}/>
                    <CollapseField title={I18n.t("about.european")}
                                   info={I18n.t("about.europeanInfo")}/>
                </div>
                <div className="card bottom">
                    <h5>
                        {I18n.t("about.manage")}
                    </h5>
                    <p className="info"
                       dangerouslySetInnerHTML={{__html: I18n.t("about.manageInfo", {url: config.spBaseUrl})}}/>
                </div>
            </div>
        </div>
    );
}