import I18n from "../locale/I18n.js";
import "./Verify.scss";
import {Verify_NL} from "./Verify_NL.jsx";
import {Verify_EN} from "./Verify_EN.jsx";

export const Verify = () => {
    return <>
        {I18n.locale === "nl" && <Verify_NL/>}
        {I18n.locale === "en" && <Verify_EN/>}
    </>
}
