import I18n from "../locale/I18n.js";
import "./Support.scss"
import {Support_NL} from "./Support_NL.jsx";
import {Support_EN} from "./Support_EN.jsx";

export const Support = () => {
    return <>
        {I18n.locale === "nl" && <Support_NL/>}
        {I18n.locale === "en" && <Support_EN/>}
    </>
}
