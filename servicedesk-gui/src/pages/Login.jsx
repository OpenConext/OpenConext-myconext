import React, {useEffect, useState} from "react";
import {Button, ButtonSize, ButtonType} from "@surfnet/sds";
import './Login.scss';
import I18n from "../locale/I18n";
import DOMPurify from "dompurify";
import FrontDesk from "../icons/frontdesk.svg";
import {LandingInfo} from "../components/LandingInfo";
import {configuration} from "../api";

export const Login = () => {

    const [isAnimating, setIsAnimating] = useState(false);
    const [config, setConfig] = useState(null);

    useEffect(() => {
        configuration().then(res => {
            setConfig(res);
        });
    }, []);

    const toggleAnimation = () => {
        setIsAnimating(true);
        setTimeout(() => setIsAnimating(false), 2250);
    }

    const doLogin = () => {
        const path = encodeURIComponent(window.location.pathname || "/");
        window.location.href = `${config.loginUrl}?redirect_path=${path}`;
    }

    return (
        <div className="top-container">
            <div className="mod-login-container">
                <div className="mod-login">
                    <div className="header-left">
                        <h1 className={"header-title"}
                            dangerouslySetInnerHTML={{__html: DOMPurify.sanitize(I18n.t("landing.header.title"))}}/>
                        <Button onClick={() => doLogin()}
                                txt={I18n.t("landing.header.login")}
                                type={ButtonType.Primary}
                                size={ButtonSize.Full}/>
                        <p className={"sup"}
                           dangerouslySetInnerHTML={{__html: DOMPurify.sanitize(I18n.t("landing.header.sup"))}}/>
                    </div>
                    <div className={`header-right ${isAnimating ? "animated" : ""}`} onClick={toggleAnimation}>
                        <FrontDesk/>
                    </div>
                </div>
            </div>
            <LandingInfo/>
        </div>
    );

}
