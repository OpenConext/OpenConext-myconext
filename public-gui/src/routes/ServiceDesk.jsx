import I18n from "../locale/I18n.js";
import "./ServiceDesk.scss"
import {ServiceDesk_NL} from "./ServiceDesk_NL.jsx";
import {ServiceDesk_EN} from "./ServiceDesk_EN.jsx";

export const ServiceDesk = () => {
    return <>
        {I18n.locale === "nl" && <ServiceDesk_NL/>}
        {I18n.locale === "en" && <ServiceDesk_EN/>}
    </>
}
