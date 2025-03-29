import I18n from "../locale/I18n.js";
import "./Privacy.scss";
import {PrivacyEN} from "../terms/PrivacyEN.jsx";
import {PrivacyNL} from "../terms/PrivacyNL.jsx";

export const Privacy = () => {

    return (
        <div className="privacy-container">
            <div className="privacy">
                <div className="top">
                    <div className="top-right">
                        <h1 className="title">
                            {I18n.t("privacy.eduID")}
                        </h1>
                        <h3>{I18n.t("privacy.title")}</h3>
                    </div>
                </div>
            </div>
            {I18n.locale === "en" && <PrivacyEN/>}
            {I18n.locale === "nl" && <PrivacyNL/>}
        </div>
    );
}