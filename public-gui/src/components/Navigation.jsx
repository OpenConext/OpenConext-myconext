import {useEffect, useState} from "react";
import I18n from "../locale/I18n";
import "./Navigation.scss"
import {stopEvent} from "../utils/Utils.js";
import {Link, useNavigate} from "react-router";
import {useAppStore} from "../stores/AppStore.js";
import {Button, ButtonType} from "@surfnet/sds";
import {LanguageSelector} from "./LanguageSelector.jsx";

const tabNames = ["home", "about", "support"]

export const Navigation = ({mobile, path}) => {

    const config = useAppStore((state) => state.config);

    const [tab, setTab] = useState("");
    const navigate = useNavigate();

    useEffect(() => {
        setTab(path.substring(1));
    }, [path]);

    const doNavigate = (e, tabName) => {
        stopEvent(e);
        setTab(tabName);
        navigate(`/${tabName}`)
    }

    return (
        <div className={`desktop-navigation ${mobile ? "mobile" : ""}`}>
            {tabNames.map(tabName => <a key={tabName}
                                        href={`/${tabName}`}
                                        className={tabName === tab ? "active" : ""}
                                        onClick={e => doNavigate(e, tabName)}>
                {I18n.t(`tabs.${tabName}`)}
            </a>)}
            <Link to={"/terms"} className="terms">
                <span>{I18n.t("footer.terms")}</span>
            </Link>
            <Link to={"/privacy"} className="terms">
                <span>{I18n.t("footer.privacy")}</span>
            </Link>
            <div className="links">
                <Button type={ButtonType.Secondary}
                        onClick={() => window.location.href = `${config.spBaseUrl}`}
                        txt={I18n.t("header.mineEduID")}/>
                <Button onClick={() => window.location.href = `${config.idpBaseUrl}/register`}
                        txt={I18n.t("header.register")}/>
                <LanguageSelector/>

            </div>

        </div>
    );
}
