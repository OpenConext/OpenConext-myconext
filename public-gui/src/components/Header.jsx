import {Link, useLocation, useNavigate} from "react-router";
import eduIDLogo from "../assets/logo_eduID.svg";
import hamburger from "../assets/hamburger.svg";
import close from "../assets/close.svg";
import {stopEvent} from "../utils/Utils.js";
import "./Header.scss";
import {Navigation} from "./Navigation.jsx";
import {useEffect} from "react";

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
                <Link className="logo" to={"/"}>
                    <img src={eduIDLogo} className="logo" alt="eduID logo"/>
                </Link>
                <Navigation mobile={false} path={currentLocation.pathname}/>
                <div className="mobile-navigation">
                    {currentLocation.pathname === "/nav" &&
                        <Link className="close" to={".."}
                              onClick={e => navigateBack(e)}>
                            <img src={close} className="close" alt="close"/>
                        </Link>}
                    {currentLocation.pathname !== "/nav" &&
                        <Link className="hamburger" to={"/nav"}>
                            <img src={hamburger} className="hamburger" alt="hamburger"/>
                        </Link>}
                </div>
            </div>
        </div>
    );
}

