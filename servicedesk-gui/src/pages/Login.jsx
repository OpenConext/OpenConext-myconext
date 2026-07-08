import React, {useEffect, useState} from "react";
import {Button, ButtonSize, ButtonType, Modal} from "@surfnet/sds";
import './Login.scss';
import I18n from "../locale/I18n";
import DOMPurify from "dompurify";
import FrontDesk from "../icons/frontdesk.svg";
import {LandingInfo} from "../components/LandingInfo";
import {configuration, logout} from "../api";
import {useSearchParams} from "react-router-dom";

export const Login = () => {

    const [isAnimating, setIsAnimating] = useState(false);
    const [config, setConfig] = useState(null);
    const [forceReauth, setForceReauth] = useState(false);

    const [searchParams, setSearchParams] = useSearchParams();
    const unauthorized = searchParams.get("unauthorized");

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
        const loginUrl = new URL(config.loginUrlServiceDesk);
        loginUrl.searchParams.set("redirect_path", "/");
        loginUrl.searchParams.set("registration_id", "service_desk");
        if (forceReauth) {
            loginUrl.searchParams.set("force", "true");
        }
        window.location.href = loginUrl.toString();
    }

    const clearSearchParams = () => {
        setSearchParams({});
        setForceReauth(true);
        logout();
    };

    return (
        <div className="top-container">
            {unauthorized && <Modal title={I18n.t("login.unauthorized")}
                                    close={clearSearchParams}
                                    focusConfirm={true}
                                    question={I18n.t("login.unauthorizedInfo")}
                                    confirmationButtonLabel={"Ok"}
                                    confirm={clearSearchParams}/>}
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
