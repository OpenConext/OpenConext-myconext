import parachute from "../assets/parachute.svg";
import I18n from "../locale/I18n.js";
import "./Support.scss"

export const Support = () => {
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
            </div>
        </div>
    );
}