import I18n from "../locale/I18n.js";
import "./Privacy.scss";
import {PrivacyEN} from "../terms/PrivacyEN.jsx";
import {PrivacyNL} from "../terms/PrivacyNL.jsx";
import {useEffect} from "react";

export const Privacy = () => {

    useEffect(() => {
        window.scroll(0, 0);
    }, []);

    return (
        <div className="privacy-container">
            <div className="privacy">
                <h1 className="title small">
                    {I18n.t("privacy.eduID")}
                </h1>
                <h2>{I18n.t("privacy.title")}</h2>
            </div>
            {I18n.locale === "en" && <PrivacyEN/>}
            {I18n.locale === "nl" && <PrivacyNL/>}
        </div>
    );
}