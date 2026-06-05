import I18n from "../locale/I18n.js";
import "./About.scss";
import {About_NL} from "./About_NL.jsx";
import {About_EN} from "./About_EN.jsx";

export const About = () => {
    return <>
        {I18n.locale === "nl" && <About_NL/>}
        {I18n.locale === "en" && <About_EN/>}
    </>
}
