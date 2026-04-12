import {Link, useNavigate} from "react-router";
import eduIDLogo from "../assets/logo_eduID.svg";
import hamburger from "../assets/hamburger.svg";
import close from "../assets/close.svg";
import {stopEvent} from "../utils/Utils.js";
import "./Header.scss";
import {Navigation} from "./Navigation.jsx";
import {useEffect} from "react";
import I18n from "../locale/I18n.js";

export const Header = ({currentLocation}) => {

    const navigate = useNavigate();

    useEffect(() => {
        //force re-render on location change
    }, [currentLocation]);

    const navigateBack = e => {
        stopEvent(e);
        navigate(-1);
    }

    return (
        <div className="header-container">
            <div className="header-inner">
                <Link className="logo" to={"/"} aria-label={I18n.t("header.home")}>
                    <img src={eduIDLogo} className="logo" alt=""/>
                </Link>
                <Navigation mobile={false} path={currentLocation.pathname}/>
                <div className="mobile-navigation">
                    {currentLocation.pathname === "/nav" &&
                        <Link className="close" to={".."} aria-label={I18n.t("header.closeMenu")}
                              onClick={e => navigateBack(e)}>
                            <img src={close} className="close" alt=""/>
                        </Link>}
                    {currentLocation.pathname !== "/nav" &&
                        <Link className="hamburger" to={"/nav"} aria-label={I18n.t("header.menu")}>
                            <img src={hamburger} className="hamburger" alt=""/>
                        </Link>}
                </div>
            </div>
        </div>
    );
}
