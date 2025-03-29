import I18n from "../locale/I18n.js";
import "./Terms.scss";
import {TermsEN} from "../terms/TermsEN.jsx";
import {TermsNL} from "../terms/TermsNL.jsx";

export const Terms = () => {

    return (
        <div className="terms-container">
            <div className="terms">
                <h1 className="title small">
                    {I18n.t("terms.eduID")}
                </h1>
                <h3>{I18n.t("terms.title")}</h3>
            </div>
            {I18n.locale === "en" && <TermsEN/>}
            {I18n.locale === "nl" && <TermsNL/>}
        </div>
    );
}