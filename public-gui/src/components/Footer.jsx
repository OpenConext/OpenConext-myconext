import I18n from "../locale/I18n";
import "./Footer.scss"
import {LanguageSelector} from "./LanguageSelector";
import {Logo, LogoType} from "@surfnet/sds";
import {Link} from "react-router";

export const Footer = () => {

    return (
        <footer className="sds--footer sds--footer--single-bar">
            <div className="sds--page-container">
                <div className="sds--footer--inner">
                    <nav className="menu sds--text--body--small">
                        <ul>
                            <li>
                                <Link to={"/terms"}>
                                    <span>{I18n.t("footer.terms")}</span>
                                </Link>
                            </li>
                            <li>
                                <Link to={"/privacy"}>
                                    <span>{I18n.t("footer.privacy")}</span>
                                </Link>
                            </li>
                        </ul>
                    </nav>
                    <LanguageSelector/>
                    <div className="sds--branding">
                        <a href={I18n.t("footer.surfLink")} target="_blank"
                           rel="noopener noreferrer">{<Logo label={""} position={LogoType.Bottom}/>}</a>
                    </div>
                </div>
            </div>
        </footer>
    );
}
