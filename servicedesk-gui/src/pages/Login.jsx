import React from "react";
import {Button, ButtonSize, ButtonType} from "@surfnet/sds";
import './Login.scss';
import I18n from "../locale/I18n";
import DOMPurify from "dompurify";
import students from "../icons/chatgpt/openart-image_raw.jpg";
import {LandingInfo} from "../components/LandingInfo.jsx";

export const Login = () => {

    const doLogin = () => {
        const path = window.location.origin;
        const encodedQueryParams = `target=${encodeURIComponent("/startSSO")}&redirect_url=${encodeURIComponent(path + "/home")}`
        window.location.href = `${path}/Shibboleth.sso/Login?${encodedQueryParams}`;
    }

    return (
        <div className="top-container">
            <div className="mod-login-container">
                <div className="mod-login">
                    <div className="header-left">
                        <h2 className={"header-title"}
                            dangerouslySetInnerHTML={{__html: DOMPurify.sanitize(I18n.t("landing.header.title"))}}/>
                        <Button onClick={() => doLogin()}
                                txt={I18n.t("landing.header.login")}
                                type={ButtonType.Primary}
                                size={ButtonSize.Full}/>
                        <p className={"sup"}
                           dangerouslySetInnerHTML={{__html: DOMPurify.sanitize(I18n.t("landing.header.sup"))}}/>
                    </div>
                    <div className="header-right">
                        <img className="screen" src={students} alt="logo"/>
                    </div>
                </div>
            </div>
            <LandingInfo/>
        </div>
    );

}