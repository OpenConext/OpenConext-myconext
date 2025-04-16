import parachute from "../assets/parachute.svg";
import I18n from "../locale/I18n.js";
import "./Support.scss"
import support from "../assets/support.svg";
import mobileScreenshot from "../assets/mobile_screenshot.svg";
import {Background} from "../components/Background.jsx";
import {useAppStore} from "../stores/AppStore.js";

export const Support = () => {

    const config = useAppStore((state) => state.config);

    return (
        <div className="support-container">
            <div className="support">
                <div className="top">
                    <img src={parachute} className="parachute" alt="parachute"/>
                    <div className="top-right">
                        <h1 className="title">
                            {I18n.t("support.eduID")}
                        </h1>
                        <h3>{I18n.t("support.title")}</h3>
                    </div>
                </div>
            </div>
            <Background>
                <div className="card row">
                    <img src={support} className="support" alt="support"/>
                    <div className="top-right middle">
                        <h2 className="title">
                            {I18n.t("support.studying")}
                        </h2>
                        <h2 className="title green">
                            {I18n.t("support.connect")}
                        </h2>
                        <p className="info"
                           dangerouslySetInnerHTML={{__html: I18n.t("support.studyInfo")}}/>
                        <ul>
                            <li>{I18n.t("support.informationBullet1")}</li>
                            <li>{I18n.t("support.informationBullet2")}</li>
                            <li>{I18n.t("support.informationBullet3")}</li>
                        </ul>
                        <p className="info"
                           dangerouslySetInnerHTML={{__html: I18n.t("support.allAtOnce", {url: config.spBaseUrl})}}/>
                        <img src={mobileScreenshot} className="mobileScreenshot" alt="mobileScreenshot"/>
                        <p className="info cursive"
                           dangerouslySetInnerHTML={{__html: I18n.t("support.note")}}/>
                    </div>
                </div>
                <div className="card bottom full">
                    <h5>
                        {I18n.t("support.help")}
                    </h5>
                    <p dangerouslySetInnerHTML={{__html: I18n.t("support.helpInfo")}}/>
                </div>
            </Background>
        </div>
    );
}